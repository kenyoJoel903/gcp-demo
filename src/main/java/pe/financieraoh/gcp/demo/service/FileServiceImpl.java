package pe.financieraoh.gcp.demo.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pe.financieraoh.gcp.demo.exception.BadRequestException;
import pe.financieraoh.gcp.demo.exception.GCPFileUploadException;
import pe.financieraoh.gcp.demo.model.FileDto;
import pe.financieraoh.gcp.demo.model.FileResponse;
import pe.financieraoh.gcp.demo.util.GcpStorageUtil;

@Service
public class FileServiceImpl implements FileService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);
	
	@Autowired
	private GcpStorageUtil gcpStorageUtil;

	@Override
	public List<FileResponse> uploadFiles(MultipartFile[] files) {
		List<FileResponse> inputFiles = new ArrayList<>();
		Arrays.asList(files).forEach(file -> {
            String originalFileName = file.getOriginalFilename();
            if(originalFileName == null){
                throw new BadRequestException("Original file name is null");
            }
            Path path = new File(originalFileName).toPath();

            try {
                String contentType = Files.probeContentType(path);
                FileDto fileDto = gcpStorageUtil.uploadFile(file, originalFileName, contentType);

                if (fileDto != null) {
                    inputFiles.add(new FileResponse(fileDto.getFileName(), fileDto.getFileUrl()));
                    LOGGER.debug("File uploaded successfully, file name: {} and url: {}",fileDto.getFileName(), fileDto.getFileUrl() );
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while uploading. Error: ", e);
                throw new GCPFileUploadException("Error occurred while uploading");
            }
        });
		return inputFiles;
	}
	
	@Override
	public Object[] dowloadFile(String fileName) {
		return gcpStorageUtil.download(fileName);
	}

}

//https://github.com/raviyasas/springboot-gcs-demo/blob/master/src/main/java/com/app/service/FileServiceImpl.java
