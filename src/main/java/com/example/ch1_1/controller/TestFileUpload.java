package com.example.ch1_1.controller;
/*
 * @author xupeng
 * @date 2022$ 0:07$
 */



import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

@Controller
public class TestFileUpload {
    @RequestMapping("/uploadFile")
    public String uploadFile() {
        return "uploadFile";
    }

    /*上传文件自动绑定到MultipartFile对象中
     *
     * 在这里使用处理方法的形参接受请求参数*/
    @RequestMapping("/upload")
    public String upload(HttpServletRequest request,
                         @RequestParam("description") String description,//文件描述
                         @RequestParam("myFile") MultipartFile myFile)
            throws IllegalStateException, IOException {
        System.out.println("文件描述：" + description);
        //如果选择上传了文件，将文件上传到指定的目录：uploadFile
        if (!myFile.isEmpty()) {
            //上传文件路径
            String path = request.getServletContext().getRealPath("/uploadFiles/");
            //获取文件的原名字
            String fileName = myFile.getOriginalFilename();
            //File.separator 代表系统目录的中间间隔符
            File filepath = new File(path + File.separator + fileName);
            //445
            if (!filepath.getParentFile().exists()) {
                filepath.getParentFile().mkdir();

            }
            //将上传文件保存到一个目标文件中
            myFile.transferTo(filepath);

        }
        return "forward:/showDownLoad";

    }

    //显示要下载的文件
    @RequestMapping("/showDownLoad")
    public String showDownLoad(HttpServletRequest request, Model model) {
        String path = request.getServletContext().getRealPath("/uploadFiles/");
        File fileDir = new File(path);
        //从指定目录获得文件
        File filesList[] = fileDir.listFiles();
        model.addAttribute("filesList", filesList);
        return "showFile";

    }

    @RequestMapping("/download")
    public ResponseEntity<byte[]> download(HttpServletRequest request,
                                           @RequestParam("filename") String filename,
                                           @RequestHeader("User-Agent") String userAgent) throws IOException {
        //下载文件路径
        String path = request.getServletContext().getRealPath("/uploadFiles/");
        //构建将要下载的文件对象
        File downFile = new File(path + File.separator + filename);
        //ok表示HTTP中的状态是200
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        //内容长度
        builder.contentLength(downFile.length());
        //APPLICATION_OCTET_STREAM:二进制流水据，最常见的文件下载
        builder.contentType(MediaType.APPLICATION_OCTET_STREAM);
        //使用URLEncoder.encode对文件进行编码
        filename = URLEncoder.encode(filename, "UTF-8");
        /*
         * 设置实际的响应文件名，告诉浏览器文件要用于“下载”和“保存”
         * 不同的浏览器，处理方式不同，根据浏览器的实际情况区别对待
         * */
        if (userAgent.indexOf("MSIE") > 0) {
            builder.header("Content-Disposition", "attachment; filename =" + filename);

        } else {
            /*
             * 非IE浏览器，如firefox,chrom等浏览器，则需要说明编码的字符集
             * filename后有一个*号，在UTF-8后面有两个但引号
             * */
            builder.header("Content-Disposition", "attachment; filename * =UTF-8''" + filename);
        }
        return builder.body(FileUtils.readFileToByteArray(downFile));
    }

}


