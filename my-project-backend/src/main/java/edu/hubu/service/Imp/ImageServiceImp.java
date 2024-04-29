package edu.hubu.service.Imp;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import edu.hubu.entity.dto.AccountDto;
import edu.hubu.entity.vo.response.AccountVO;
import edu.hubu.mapper.AccountMapper;
import edu.hubu.service.ImageService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
@Slf4j
@Service
public class ImageServiceImp implements ImageService {
    @Resource
    MinioClient minioClient;
    @Resource
    AccountMapper accountMapper;
    final String IMAGE_PARTITION = "/avatar/";
    final String IMAGE_BUCKET = "study";
    @Transactional
    @Override
    public String uploadAvatar(MultipartFile file, int id) throws IOException {
        String imageName = UUID.randomUUID().toString().replace("-","");
        imageName = IMAGE_PARTITION + imageName;
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(IMAGE_BUCKET)
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(imageName)
                .build();

        try {
            minioClient.putObject(args);
            if(accountMapper.update(null, Wrappers.<AccountDto>update()
                    .eq("id",id).set("avatar",imageName))>0){
                return imageName;
            }else {
                return null;
            }
        } catch (Exception e){
            log.error("图片上传出现问题："+e.getMessage());
            return null;
        }
    }
    @Override
    public void fetchImageFromMinio(OutputStream stream,String image) throws Exception{
    GetObjectArgs args = GetObjectArgs.builder()
                .bucket(IMAGE_BUCKET)
                .object(image)
                .build();
        GetObjectResponse response = minioClient.getObject(args);
        IOUtils.copy(response,stream);
    }
}
