package pe.financieraoh.gcp.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pe.financieraoh.gcp.demo.model.FileResponse;
import pe.financieraoh.gcp.demo.service.FileService;

@RestController
@RequestMapping("/api/gcp/storage")
public class GcpStorageController {
	
	@Autowired
	private FileService fileService;
	
	@PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public List<FileResponse> addFile(@RequestParam("file") MultipartFile file){
        return fileService.uploadFiles(new MultipartFile[] {file});
    }
	
	@GetMapping(value = "/download")
	public ResponseEntity<?> download(@RequestParam String fileName) {
		Object[] file = fileService.dowloadFile(fileName);
		if(file != null) {
			return ResponseEntity.ok().header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
					.body((byte[])file[0]);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
