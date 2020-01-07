package mystery.framework.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.xx7x.usual.support.ParameterService;
import mystery.framework.AppConfig;
import mystery.framework.Globals;
import mystery.framework.model.FileEntity;

import org.apache.commons.fileupload.FileItem;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.xx7x.usual.support.AppConstants;

public class UploadUtil {
	private static String uploadPath = null;
	private static String imagesType = null;
	private static String breviaryMaxImgSize = null;
	private static String breviaryMaxImageEndName = null;
	private static String breviaryMinImageEndName = null;
	private static String breviaryMinImgSize = null;
	private List<FileEntity> uploadFiles = new ArrayList<FileEntity>();

	
	public List<FileEntity> upload(HttpServletRequest request,boolean check){
		CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(request.getSession().getServletContext());
		FileEntity result=null;
        //检查form中是否有enctype="multipart/form-data"
        if(multipartResolver.isMultipart(request)){
            //将request变成多部分request
            MultipartHttpServletRequest multiRequest=(MultipartHttpServletRequest)request;
           //获取multiRequest 中所有的文件名
            Iterator iter=multiRequest.getFileNames();
            FileEntity entity;
            while(iter.hasNext()){
                MultipartFile file=multiRequest.getFile(iter.next().toString());
                if(file!=null){
                	entity = upload(file,check);
                	uploadFiles.add(entity);
                }
            }
        }
		return uploadFiles;
	}
	
	
	public FileEntity upload(MultipartFile file,boolean check){
		String targetDirectory = "";
		FileEntity entity = new FileEntity();
		if(check){
			//校验上传文件类型和大小,防止非法上传
			String type=file.getContentType();
			long size=file.getSize();
			if(!"image/jpg".equals(type)&&!"image/jpeg".equals(type)&&!"image/png".equals(type)){
				entity.setResult(AppConstants.SCBJ_DELETE);
				return entity;
			}
			if(size>2*1024*1024){
				entity.setResult(AppConstants.SCBJ_DELETE);
				return entity;
			}
		}
		try{
			init();
			String realName = "", realRand = "", fileType = "";
			String ymStr = DateUtil.getYM();
			targetDirectory = uploadPath + ymStr + "/";// 上传文件前以年月文件夹
			String realDirectory = Globals.getFilePath(targetDirectory);
			FileUtil.createDir(realDirectory);
			entity.setContentType(file.getContentType());
			entity.setFileSize(file.getSize());
			entity.setFileName(file.getOriginalFilename());
			realRand = StringUtil.getRandom();
			fileType = getExt(entity.getFileName());
			realName = realRand + fileType;
			entity.setRealName(ymStr + "/" + realName);
			File fileInfo=new File(realDirectory + realName);
			file.transferTo(fileInfo);
			try {
				String prefix= ParameterService.get().getInnerAddress();
				String url=prefix+"/services/office/automation/upload.do";
				String info=HttpFileUtil.postStream(url,fileInfo.getPath(),file.getOriginalFilename());
				if(StringUtil.isNotEmpty(info)){
					entity=FastJsonUtils.jsonToBean(info,FileEntity.class);
				}
				FileUtil.deleteFile(fileInfo.getPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}catch(Exception err){
			err.getStackTrace();
		}
		return entity;
	}

	public List<FileEntity> upload(List<FileItem> upload, String[] uploadFileName,
			String[] uploadContentType) throws IOException {
		String targetDirectory = "";
		init();
		String fileName = "", type = "", realName = "", realRand = "", fileType = "";
		String ymStr = DateUtil.getYM();
		targetDirectory = uploadPath + ymStr + "/";// 上传文件前以年月文件夹
		String realDirectory = Globals.getFilePath(targetDirectory);
		FileUtil.createDir(realDirectory);
		for (int i = 0; i < upload.size(); i++) {
			if (!upload.get(i).isFormField()) {
				if (upload.get(i)== null || upload.size()<=0)
					continue;
				fileName = uploadFileName[i];// 上传的文件名
				/*type = uploadContentType[i];// 文件类型*/
				if (fileName == null || fileName.equals(""))
					continue;
				realRand = StringUtil.getRandom();
				fileType = getExt(fileName);
				realName = realRand + fileType;
				File target = new File(realDirectory, realName);
				InputStream is = upload.get(i).getInputStream();
				OutputStream os = new FileOutputStream(target);
				byte[] buffer = new byte[2048];
				int length = 0;
				Integer size = 0;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
					size += length;
				}
				is.close();
				os.close();// 上传至服务器的目录，一般都这样操作，
				// 在把路径写入数据库即可
				if (toThumbnail(fileType)) {// 当上传为图片时，生成缩略图
					ImageUtil imageUtil = new ImageUtil();
					imageUtil.scale(realDirectory + realName, realDirectory
							+ realRand + breviaryMaxImageEndName + fileType,
							Integer.valueOf(breviaryMaxImgSize), 0);
					//生成缩略图2
					imageUtil.scale(realDirectory + realName, realDirectory
							+ realRand + breviaryMinImageEndName + fileType,
							Integer.valueOf(breviaryMinImgSize), 0);
				}
				FileEntity uf = new FileEntity();// 创建文件
				uf.setContentType(fileType);
				uf.setFileName(fileName);
				uf.setRealName(targetDirectory + realName);
				uf.setFileSize(size);
				uploadFiles.add(uf);// 添加到需要下载文件的List集合中

			}
		}
		return uploadFiles;
	}

	//获取缩略图地址
	public  String getThumbnailUrl(HttpServletRequest request,String realName,String flag){
		int indexOf = realName.indexOf(".");
		CharSequence subSequence = realName.subSequence(0, indexOf);
		String surl =null;
		if(flag=="0"){	//获取小缩略图
			surl = subSequence+AppConstants.MinBreviary;
		}
		if(flag=="1"){	//获取大缩略图
			surl = subSequence+AppConstants.MaxBreviary;
		}
		String substring = realName.substring(indexOf);
		String fileUrl="";
		if(flag==""){
			 fileUrl =subSequence+ substring;
		}else{
			fileUrl = surl + substring;
		}
		String path =request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
		String contextPath = request.getContextPath();
		String url = path+contextPath+"//upFile//"+fileUrl;
		return url;
	}
	
	
	private void init() {
		if (uploadPath == null)
			uploadPath = AppConfig.getInstance().getKey("UpLoadPath");
		if (imagesType == null)
			imagesType = AppConfig.getInstance().getKey("ImagesType");
		if (breviaryMaxImgSize == null)
			breviaryMaxImgSize = AppConfig.getInstance().getKey("BreviaryMaxImgSize");
		if (breviaryMaxImageEndName == null)
			breviaryMaxImageEndName = AppConfig.getInstance().getKey("BreviaryMaxImageEndName");
		if (breviaryMinImgSize == null)
			breviaryMinImgSize = AppConfig.getInstance().getKey("BreviaryMinImgSize");
		if (breviaryMinImageEndName == null)
			breviaryMinImageEndName = AppConfig.getInstance().getKey("BreviaryMinImageEndName");
	}

	public static String getExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf("."));
	}
	
	protected String formatType(String type){
		String tStr = "";
		type = type.toLowerCase();
		if(type.equals(".jpeg"))
			tStr = "image/jpeg";
		else if(type.equals(".jpg"))
			tStr = "image/jpg";
		else if(type.equals(".bmp"))
			tStr = "image/bmp";
		else if(type.equals(".xpng"))
			tStr = "image/x-png";
		else if(type.equals(".png"))
			tStr = "image/png";
		else if(type.equals(".gif"))
			tStr = "image/gif";
		else  if(type.equals(".pjpeg"))
			tStr = "image/pjpeg";
		return tStr;
	}
	
	protected boolean toThumbnail(String type){
		type = formatType(type);
		if(StringUtil.isEmpty(type)||
			imagesType.indexOf(type) < 0)
			return false;
		else
			return true;
	}
	
}
