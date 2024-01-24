package com.example.coupleapp.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.coupleapp.config.S3Config;
import com.example.coupleapp.entity.ImageEntity;
import com.example.coupleapp.exception.domian.CommonErrorCode;
import com.example.coupleapp.exception.domian.CommonException;
import com.example.coupleapp.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService {

    private final AmazonS3Client amazonS3Client;
    private S3Config s3Config;
    private final ImageRepository imageRepository;

    @Value("${S3_BUCKET_NAME}")
    private String bucket;

    public String uploadImageFile(MultipartFile imageFile) {
        String fileName = createFileName(imageFile.getOriginalFilename());
        try{
            if(imageFile.isEmpty()) {
                throw new CommonException(CommonErrorCode.NOT_FOUND_IMG_FILES);
            }
            return memoryUpload(imageFile,fileName);
        } catch (IOException e) {
            throw new CommonException(CommonErrorCode.FAIL_TO_SAVE);
        }
    }

    public List<String> uploadImageFileList(List<MultipartFile> imgList) throws IOException {
        // TODO 이미지 null 시 예외처리
        if(imgList == null) throw new CommonException(CommonErrorCode.NOT_FOUND_IMG_FILES);

        List<String> urlList = new ArrayList<>();
        imgList.forEach(multipartFile -> {
            String fileName = createFileName(multipartFile.getOriginalFilename());
            try {
                String url = memoryUpload(multipartFile, fileName);
                urlList.add(url);
            } catch (IOException e) {
                throw new CommonException(CommonErrorCode.FAIL_TO_SAVE);
            }

        });
        return urlList;
    }

    public String memoryUpload(MultipartFile uploadFile,String fileName) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(uploadFile.getSize());
        objectMetadata.setContentType(uploadFile.getContentType());

        try(InputStream inputStream = uploadFile.getInputStream()) {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucket,fileName,inputStream,objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );
            return amazonS3Client.getUrl(bucket,fileName).toString();
        }
    }
    public String createFileName(String fileName) {
        return UUID.randomUUID().toString();
    }
    public String saveImageToS3AndDatabase(MultipartFile uploadFile) throws IOException {
        String fileName = createFileName(uploadFile.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(uploadFile.getSize());
        objectMetadata.setContentType(uploadFile.getContentType());

        try (InputStream inputStream = uploadFile.getInputStream()) {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );

            // 이미지 정보를 데이터베이스에 저장
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setFileName(fileName);
            imageEntity.setImageData(amazonS3Client.getUrl(bucket, fileName).toString().getBytes());

            imageRepository.save(imageEntity);

            return amazonS3Client.getUrl(bucket, fileName).toString();
        } catch (IOException e) {
            throw new CommonException(CommonErrorCode.FAIL_TO_SAVE);
        }
    }
    }
//        // 이미지 파일 이름이 중복되지 않게 uuid 생성
//        String fileName = file.getOriginalFilename();
//        String ext = fileName.substring(fileName.indexOf("."));
//
//        String uuidFileName = UUID.randomUUID() + ext;
//
//        // 서버에 저장할 경로 생성
//        String localPath = localLocation + uuidFileName;
//
//        // 서버 환경에 이미지 파일 저장
//        File localFile = new File(localPath);
//        file.transferTo(localFile);
//
//        // s3에 이미지 업로드
//        s3Config.amazonS3Client()
//                .putObject(new PutObjectRequest(bucket,uuidFileName,localFile)
//                        .withCannedAcl(CannedAccessControlList.PublicRead));
//        String s3Url = s3Config.amazonS3Client().getUrl(bucket,uuidFileName).toString();
//
//        // 서버에 저장한 이미지를 삭제
//        localFile.delete();
//

//        return s3Url;

//    }

//    private String getFileExtension(String fileName) {
//        try {
//            String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
//            if (extension.equals(".png") || extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".webp")) {
//                return extension;
//            }
//            throw new CommonException(CommonErrorCode.NOT_IMAGE_EXTENSION);
//        }catch (StringIndexOutOfBoundsException e) {
//            throw new CommonException(CommonErrorCode.INVALID_FORMAT_FILE);
//        }
//    }



