package pe.financieraoh.gcp.demo.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import pe.financieraoh.gcp.demo.exception.BadRequestException;
import pe.financieraoh.gcp.demo.exception.FileWriteException;
import pe.financieraoh.gcp.demo.exception.GCPFileUploadException;
import pe.financieraoh.gcp.demo.exception.InvalidFileTypeException;
import pe.financieraoh.gcp.demo.model.FileDto;

//import net.bytebuddy.utility.RandomString;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

@Component
public class GcpStorageUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(GcpStorageUtil.class);

	@Value("${gcp.config.file}")
	private String gcpConfigFile;

	@Value("${gcp.project.id}")
	private String gcpProjectId;

	@Value("${gcp.bucket.id}")
	private String gcpBucketId;

	public void upload() {

	}

	public FileDto uploadFile(MultipartFile multipartFile, String fileName, String contentType) {

		try {

			LOGGER.debug("Start file uploading process on GCS");
			byte[] fileData = FileUtils.readFileToByteArray(convertFile(multipartFile));

			InputStream inputStream = new ClassPathResource(gcpConfigFile).getInputStream();

			StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
					.setCredentials(GoogleCredentials.fromStream(inputStream)).build();

			Storage storage = options.getService();
			Bucket bucket = storage.get(gcpBucketId, Storage.BucketGetOption.fields());

			UUID uuid = UUID.randomUUID();
			Blob blob = bucket.create(fileName + "-" + uuid.toString() + checkFileExtension(fileName), fileData,
					contentType);

			if (blob != null) {
				LOGGER.debug("File successfully uploaded to GCS");
				return new FileDto(blob.getName(), blob.getMediaLink());
			}

		} catch (Exception e) {
			LOGGER.error("An error occurred while uploading data. Exception: ", e);
			throw new GCPFileUploadException("An error occurred while storing data to GCS");
		}
		throw new GCPFileUploadException("An error occurred while storing data to GCS");
	}

	public Object[] download(String objectName) {
		try {
			Object[] data = new Object[2];
			InputStream inputStream = new ClassPathResource(gcpConfigFile).getInputStream();

			StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
					.setCredentials(GoogleCredentials.fromStream(inputStream)).build();

			Storage storage = options.getService();
			
			Blob blob = storage.get(gcpBucketId, objectName);
			if (blob != null) {
				File fileDestination = File.createTempFile(UUID.randomUUID().toString() + "_file_tmp", null);
				blob.downloadTo(fileDestination.toPath());
				data[0] = Files.readAllBytes(fileDestination.toPath());
				data[1] = getContentType(objectName);
			}
			return data;
		} catch (Exception e) {
			LOGGER.error("[Error] download ", e);
			return null;
		}
	}

	private File convertFile(MultipartFile file) {

		try {
			if (file.getOriginalFilename() == null) {
				throw new BadRequestException("Original file name is null");
			}
			File convertedFile = new File(file.getOriginalFilename());
			FileOutputStream outputStream = new FileOutputStream(convertedFile);
			outputStream.write(file.getBytes());
			outputStream.close();
			LOGGER.debug("Converting multipart file : {}", convertedFile);
			return convertedFile;
		} catch (Exception e) {
			throw new FileWriteException("An error has occurred while converting the file");
		}
	}

	private String checkFileExtension(String fileName) {
		if (fileName != null && fileName.contains(".")) {
			String[] extensionList = { ".png", ".jpeg", ".pdf", ".doc", ".mp3", ".sql", ".json", ".txt" };

			for (String extension : extensionList) {
				if (fileName.endsWith(extension)) {
					LOGGER.debug("Accepted file type : {}", extension);
					return extension;
				}
			}
		}
		LOGGER.error("Not a permitted file type");
		throw new InvalidFileTypeException("Not a permitted file type");
	}

	private String getContentType(String fileName) {
		fileName = fileName.toLowerCase();
		if (fileName.contains("jpeg"))
			return "image/jpeg";
		if (fileName.contains("icon"))
			return "image/x-icon";
		if (fileName.contains("svg"))
			return "image/svg+xml";
		if (fileName.contains("tif"))
			return "image/tiff";
		if (fileName.contains("jpg"))
			return "image/jpg";
		if (fileName.contains("png"))
			return "image/png";
		if (fileName.contains("pdf"))
			return "application/pdf";
		return "application/octet-stream";
	}

}
