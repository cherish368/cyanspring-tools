package mystery.framework.utils;

import java.util.Map;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xx7x.usual.support.CacheVoids;

import mystery.framework.AppConfig;
import mystery.framework.Globals;
import mystery.framework.base.ExcelCell;

public class ExcelUtil<T> {
	protected static Log log = LogFactory.getLog(ExcelUtil.class);
	protected String XLS_WORKBOOK_LOCATION = null;
	protected HSSFWorkbook workbook;
	protected HSSFSheet sheet;
	protected List<HSSFSheet> sheets;

	public ExcelUtil() {
		workbook = new HSSFWorkbook(); // 产生工作簿对象
		XLS_WORKBOOK_LOCATION = String.format(AppConfig.getInstance().getKey("TmpPath"), StringUtil.getRandom());
		sheet = workbook.createSheet("Sheet1"); // 产生工作表对象
	}

	public ExcelUtil(String path, String fileName) {
		XLS_WORKBOOK_LOCATION = String.format(AppConfig.getInstance().getKey(path), fileName);
		workbook = new HSSFWorkbook(); // 产生工作簿对象
		sheet = workbook.createSheet("Sheet1"); // 产生工作表对象
	}
	
	public ExcelUtil(boolean type) {
		workbook = new HSSFWorkbook(); 
		XLS_WORKBOOK_LOCATION = String.format(AppConfig.getInstance().getKey("TmpPath"),StringUtil.getRandom());
		if(!type){								
			sheet = workbook.createSheet("Sheet1"); 
		}				
	}
	
	public ExcelUtil(String fileName){
		InputStream instream = null;
		try{
			instream = new FileInputStream(fileName);
			workbook = new HSSFWorkbook(instream);
		}catch(IOException err){
			log.error(err);
		}finally{
			if(instream!=null){
				try{
					instream.close();
				}catch(IOException err){
					log.error(err);
				}
			}
		}
	}

	public void createHeader(String[] titles) {
		HSSFRow row = null;
		if (titles != null && titles.length > 0) {// 添加表头
			row = sheet.createRow((short) 0);
			HSSFCell cell1;
			CellStyle cellStyle = createStyle();
			HSSFFont font = workbook.createFont();
			font.setColor(HSSFFont.COLOR_RED);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFont(font);
			
			cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex()); // 背景色
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND); 
			//cellStyle.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);

			for (int i = 0; i < titles.length; i++) {
				cell1 = row.createCell((short) i);// 第一列
				cell1.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell1.setCellValue(titles[i]);
				cell1.setCellStyle(cellStyle);
			}// ## 冻结首行 ##//
			sheet.createFreezePane(0, 1, 0, 1);
			for(int i=0;i<titles.length;i++)
				sheet.autoSizeColumn((short)i); 
		}
	}

	public void createRow(int rowNum, String[] values) {
		HSSFRow row = sheet.createRow(rowNum);
		for (int i = 0; i < values.length; i++) {
			createCell(row, i, values[i]);
		}
	}

	public CellStyle createStyle(short align, short fontSize) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(align);// 设置单元格样式
		/**注销起*/
		/*HSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setFontName("宋体");
		font.setFontHeight((short) fontSize);*/
		/**注销止*/
		cellStyle.setBorderLeft(CellStyle.BORDER_THIN); 
		cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderRight(CellStyle.BORDER_THIN); 
		cellStyle.setRightBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderBottom(CellStyle.BORDER_THIN); 
		cellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderTop(CellStyle.BORDER_THIN); 
		cellStyle.setTopBorderColor(HSSFColor.BLACK.index);
//		cellStyle.setFont(font);
		return cellStyle;
	}

	public CellStyle createStyle() {
		return createStyle(HSSFCellStyle.ALIGN_CENTER, (short) 300);
	}

	/* 创建内容单元格 */
	public void createCell(HSSFRow row, int col, String value) {
		HSSFCell cell = row.createCell(col);
		cell.setCellType(HSSFCell.ENCODING_UTF_16);
		//cell.setCellStyle(style)
		cell.setCellStyle(createStyle());
		cell.setCellValue(new HSSFRichTextString(value));
	}
	
	public void mergeRow(int start,int end,int[] cols){
		for(int i=0;i<cols.length;i++)
			sheet.addMergedRegion(new Region(start,(short) cols[i], end, (short) cols[i]));
	}

	public void createSumRow(int colSum, String[] cellsValue) {
		CellStyle cellStyle = createStyle(HSSFCellStyle.ALIGN_LEFT, (short) 250);
		HSSFRow lastRow = sheet.createRow((short) sheet.getLastRowNum() + 1);
		HSSFCell sumCell = lastRow.createCell(0);
		sumCell.setCellValue(new HSSFRichTextString("合计"));
		for (int i = 2; i < (cellsValue.length + 2); i++) {
			sumCell = lastRow.createCell(i);
			sumCell.setCellStyle(cellStyle);
			sumCell.setCellValue(new HSSFRichTextString(cellsValue[i - 2]));
		}
	}

	public String exportMap(List<Map> array, String[][] titles) {
		createHeader(titles[0]);
		String[] fields = titles[1];
		String[] values = new String[titles[1].length];
		Map map;
		for (int i = 0; i < array.size(); i++) {
			try {
				map = array.get(i);
				Object curValue;
				for (int j = 0; j < fields.length; j++) {
					if(!StringUtil.isEmpty(fields[j])){
						curValue = map.get(fields[j]);
						values[j] = getFormat(curValue);
					}
				}
				createRow((short) (i + 1), values);
			} catch (Exception err) {
				log.error(err.getMessage());
			}
		}
		export();
		return XLS_WORKBOOK_LOCATION;
	}

	protected String getFormat(Object curValue) {
		String val = "";
		if (curValue != null) {
			if (curValue instanceof Date)
				val = DateUtil.getLongDate((Date) curValue);
			else
				val = curValue.toString();
		}
		return val;
	}

	/** 获取集合中对象的属性字段。认为list中的所有对象和list中的第一个对象是同一类对象,margins为合并列 */
	private String export(List<T> array, String[][] titles, int... margins) {
		createHeader(titles[0]);
		String[] fields = titles[1];
		String[] values = new String[titles[1].length];
		T model = null, preModel = null;
		for (int i = 0; i < array.size(); i++) {
			model = array.get(i);
			Class clazz = model.getClass();
			try {
				Object curValue, preValue;
				for (int j = 0; j < fields.length; j++) {
					curValue = getFormat(clazz, fields[j], model);
					values[j] = getFormat(curValue);
				}
				createRow((short) (i + 1), values);
				if (margins!=null && margins.length > 0) {
					for (int k = 0; k < fields.length; k++) {
						for (int margin : margins) {
							if (margin == k) {
								if (preModel != null) {
									curValue = getFormat(clazz, fields[k], model);
									preValue = getFormat(clazz, fields[k], preModel);
									if (curValue.equals(preValue)) {// 合并单元格
										sheet.addMergedRegion(new Region(i,(short) k, i +1, (short) k));//包括标题
									}
								}
							}
						}
					}
				}
			} catch (Exception err) {
				log.error(err.getMessage());
			}
			preModel = model;
		}
		
		for(int i=0;i<titles[1].length;i++)
			sheet.autoSizeColumn((short)i); 
		
		export();
		return XLS_WORKBOOK_LOCATION;
	}
	/*protected Object getFormat(Class clazz, String field, T model) {
		try {
			if(field.length()>0){
				Method method = clazz.getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1));
				return method.invoke(model);
			}
			return "";
		} catch (Exception err) {
			log.error(err.getMessage());
		}
		return "";
	}*/
	protected Object getFormat(Class clazz, String field, Object model) {
		try {
			if(field.length()>0){
				Method method = clazz.getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1).toLowerCase());
				return method.invoke(model);
			}
			return "";
		} catch (Exception err) {
			log.error(err.getMessage());
		}
		return "";
	}
	
	public void export() {
		try {
			FileOutputStream fileOut = new FileOutputStream(Globals.getFilePath(XLS_WORKBOOK_LOCATION));
			workbook.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String export(List<T> array, String[][] titles,int[] subTitleNums ,String childStr,String keyStr,int... margins) {
		createHeader(titles[0]);
		String[] fields = titles[1];
		String[] values = new String[titles[1].length];
		T preModel = null;
		List childs = null;
		Object child = null;
		if(array!=null){
			int i=0;
			for (T model:array) {
				Class clazz = model.getClass();
				try {
					Object curValue, preValue;
					childs = (List) getFormat(clazz, childStr, model);
					if(childs==null||childs.size()==0){
						for (int j = 0; j < fields.length; j++) {
							curValue = getFormat(clazz, fields[j], model);
							values[j] = getFormat(curValue);
						}
						createRow((short) (i + 1), values);
						preModel = null;
						i++;
					}else{
						boolean isequal;
						for(int si = 0; si < childs.size(); si++){
							child = childs.get(si);
							for (int j = 0; j < fields.length; j++) {
								isequal = false;
								for(int num:subTitleNums){
									if(j==num)
										isequal = true;
								}
								if(isequal){
									curValue = getFormat(child.getClass(), fields[j], child);
									values[j] = getFormat(curValue);
								}else{
									curValue = getFormat(clazz, fields[j], model);
									values[j] = getFormat(curValue);
								}
							}
							createRow((short) (i + 1), values);
							if (margins!=null && margins.length > 0) {
								if (preModel != null) {
									curValue = getFormat(clazz, keyStr, model);
									preValue = getFormat(clazz, keyStr, preModel);
									if(curValue!=null && curValue.equals(preValue)){
										for (int k = 0; k < fields.length; k++) {
											for (int margin : margins) {
												if (margin == k) {
													curValue = getFormat(clazz, fields[k], model);
													preValue = getFormat(clazz, fields[k], preModel);
													if ((curValue!=null&&curValue.equals(preValue))
															||(curValue==null&&preValue==null)) {// 合并单元格
														sheet.addMergedRegion(new Region(i,(short) k, i+1, (short) k));//包括标题
													}
												}
											}
										}
									}
								}
							}
							preModel = model;
							i++;
						}
					}
				} catch (Exception err) {
					log.error(err.getMessage());
				}
			}
		}
		export();
		return XLS_WORKBOOK_LOCATION;
	}
	
	public String getLocalName(){
		return XLS_WORKBOOK_LOCATION;
	}
	
	public void resizeColumn(int count){
		for(int i=0;i<count;i++)
			sheet.autoSizeColumn((short)i);
	}
	
	public String exports(List<T> array, String[][] titles,int[] subTitleNums ,String childStr,String keyStr,int... margins) {
		if(!StringUtil.isEmpty(childStr) && !StringUtil.isEmpty(keyStr)
				&& subTitleNums!=null && subTitleNums.length>0){
			return export(array,titles,subTitleNums,childStr,keyStr,margins);
		}else
			return export(array,titles,margins);
	}
	
	public List<T> builderList(int sheet,int startRow,Class<T> clazz){
		return builderList(workbook.getSheetAt(sheet),startRow,clazz);
	}
	
	public List<T> builderList(Class<T> clazz,int sheet){
		return builderList(workbook.getSheetAt(0),0,clazz);
	}
	
	public List<T> builderList(Sheet sheet,int startRow,Class<T> clazz){
		ExcelCell excelCell;
		List<T> list = new ArrayList<T>();
		T model;
		Object tmp = null;
		Method method;
		Field[] props = clazz.getDeclaredFields();
		Cell cell;
		Class cellClass;
		int cursor=0;
		for (Row row : sheet) {
			if(cursor<startRow){
				cursor++;
				continue;
			}
			model = BeanUtil.getInstance(clazz);
			for(Field prop : props){
				try{
					excelCell = prop.getAnnotation(ExcelCell.class);
					if(excelCell==null)
						continue;
					cell = row.getCell(excelCell.index());
					if(cell==null)
						continue;
					switch (cell.getCellType()) {
	                    case Cell.CELL_TYPE_STRING:   
	                    	tmp = cell.getRichStringCellValue().getString();   
	                        break;
	                    case Cell.CELL_TYPE_NUMERIC:   
	                        if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {   
	                        	tmp = cell.getDateCellValue();
	                        }else 
	                        	tmp = cell.getNumericCellValue();
	                        break;
	                    case Cell.CELL_TYPE_BOOLEAN: 
	                    	tmp = cell.getBooleanCellValue();
	                        break;
	                    case Cell.CELL_TYPE_FORMULA:
	                    	tmp = cell.getCellFormula();
	                        break;
	                    default:   
	                    	tmp = "";
	                }
					tmp = PropertyUtil.getValue(excelCell.dataType(), tmp);
					if (tmp != null){
						cellClass = PropertyUtil.getType(excelCell.dataType());
						if(cellClass!=null){
							method = clazz.getMethod("set"+excelCell.name(), cellClass);
							if(method!=null)
								method.invoke(model, tmp);
						}
					}
				}catch (Exception err) {
					log.error(err);
				}
			}
			list.add(model);
		}
		return list;
	}

	/**
	 * 创建多个工作簿
	 * 实例化工具类注意调用ExcelUtil(boolean type);
	 * @param array
	 * @param titles
	 * @param list
	 * @param margins
	 * @return
	 */
    @SuppressWarnings("unchecked")
	public HSSFWorkbook exportMoreSheet(Map<String,Object> arrsys, Map<String,Object> map,List<String> list,int... margins) {
    	if(!ListUtil.isEmpty(list)){
    		for(String t:list){
    			String name=CacheVoids.getExCfgTableEName(t);
    			sheet = workbook.createSheet(StringUtil.isEmpty(name)?t:name);
    			String[][] titles=(String[][]) map.get(t);
    			List<T> array=(List<T>) arrsys.get(t);
    			exportMoreSheet(array,titles,sheet,margins);
    		}
    	}	    
        return workbook;
    }

	@SuppressWarnings("deprecation")
	public void exportMoreSheet(List<T> array,String[][] titles,HSSFSheet sheet,int... margins) {
        createHeader(titles[0]);
        String[] fields = titles[1];
        String[] values = new String[titles[1].length];
        T model = null, preModel = null;
        for (int i = 0; i < array.size(); i++) {
            model = array.get(i);
            Class<?> clazz = model.getClass();
            try {
                Object curValue, preValue;
                for (int j = 0; j < fields.length; j++) {
                    if(!"''".equals(fields[j])) {
                        curValue = getFormat(clazz, fields[j], model);
                        values[j] = getFormat(curValue);
                    }else{
                        values[j] = "";
                    }
                }
                createRow((short) (i + 1), values);
                if (margins!=null && margins.length > 0) {
                    for (int k = 0; k < fields.length; k++) {
                        for (int margin : margins) {
                            if (margin == k) {
                                if (preModel != null) {
										curValue = getFormat(clazz, fields[k], model);
										preValue = getFormat(clazz, fields[k], preModel);
                                    if (curValue.equals(preValue)) {// 合并单元格
                                        sheet.addMergedRegion(new Region(i,(short) k, i +1, (short) k));//包括标题
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception err) {
                log.error(err.getMessage());
            }
            preModel = model;
        }
   
    }
    /** 获取集合中对象的属性字段。认为list中的所有对象和list中的第一个对象是同一类对象,margins为合并列
	 * 返回workBook对象
	 * */
    public HSSFWorkbook exportToBs(List<T> array, String[][] titles, int... margins) {
        createHeader(titles[0]);
        String[] fields = titles[1];
        String[] values = new String[titles[1].length];
        T model = null, preModel = null;
        for (int i = 0; i < array.size(); i++) {
            model = array.get(i);
            Class clazz = model.getClass();
            try {
                Object curValue, preValue;
                for (int j = 0; j < fields.length; j++) {
                    if(!"''".equals(fields[j])) {
                        curValue = getFormat(clazz, fields[j], model);
                        values[j] = getFormat(curValue);
                    }else{
                        values[j] = "";
                    }
                }
                createRow((short) (i + 1), values);
                if (margins!=null && margins.length > 0) {
                    for (int k = 0; k < fields.length; k++) {
                        for (int margin : margins) {
                            if (margin == k) {
                                if (preModel != null) {
										curValue = getFormat(clazz, fields[k], model);
										preValue = getFormat(clazz, fields[k], preModel);
                                    if (curValue.equals(preValue)) {// 合并单元格
                                        sheet.addMergedRegion(new Region(i,(short) k, i +1, (short) k));//包括标题
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception err) {
                log.error(err.getMessage());
            }
            preModel = model;
        }

//        for(int i=0;i<titles[1].length;i++)
//            sheet.autoSizeColumn((short)i);
//        export();
        return workbook;
    }
}