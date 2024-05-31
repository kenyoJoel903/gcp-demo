package pe.financieraoh.gcp.demo.service;

import org.springframework.web.multipart.MultipartFile;

import pe.financieraoh.gcp.demo.model.FileResponse;

import java.util.List;

public interface FileService {
	
	List<FileResponse> uploadFiles(MultipartFile[] files);

	Object[] dowloadFile(String fileName);

}
