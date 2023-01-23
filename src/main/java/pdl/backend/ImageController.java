package pdl.backend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

import com.fasterxml.jackson.annotation.JacksonInject.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  @Autowired
  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
  }

  @RequestMapping(value = "/images/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<?> getImage(@PathVariable("id") long id) {
    var imgSave = imageDao.retrieve(id);
    if (imgSave.isEmpty())
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    return ResponseEntity
        .ok()
        .contentType(MediaType.IMAGE_JPEG)
        .body(imgSave.get().getData());

  }

  @DeleteMapping(value = "/images/{id}")
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {
    var imgSave = imageDao.retrieve(id);
    if (imgSave.isEmpty())
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    imageDao.delete(imgSave.get());
    return new ResponseEntity<>(id, HttpStatus.NO_CONTENT);
  }

  @PostMapping(value = "/images")
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    try {
      Image img = new Image(file.getOriginalFilename(), file.getBytes());
      imageDao.create(img);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @RequestMapping(value = "/images", produces = "application/json; charset=UTF-8")
  @ResponseBody
  public ArrayNode getImageList() {
    ArrayNode nodes = mapper.createArrayNode();
    for (Image img : imageDao.retrieveAll()) {
      ObjectNode o = mapper.createObjectNode();
      o.put("id", img.getId());
      o.put("name", img.getName());
      nodes.add(o);
    }
    return nodes;
  }

}
