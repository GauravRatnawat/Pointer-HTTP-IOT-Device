package com.main;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class Hashh 
{

    public static void main(String[] args) throws Exception
    {
    	
    	List<String> registerIdList=new LinkedList<String>();
    	
    	
//         System.out.println("Default Charset=" + Charset.defaultCharset());
//         System.out.println("file.encoding=" +System.getProperty("file.encoding"));
//         System.out.println("Default Charset=" + Charset.defaultCharset());
//         System.out.println("Default Charset in Use=" +getDefaultCharSet());
//         String s = "Hello World";
//         
//     	ByteBuffer buf=null;
//		 buf = ByteBuffer.wrap((s).getBytes("UTF8"));
//		 
//		 System.out.println(new String(buf.array(), "UTF8"));
//         byte arr[] = s.getBytes("UTF8");
//         for (byte x: arr) {
//            System.out.print(x+" ");
//         }
//         System.out.println(new String("Gaurav","utf-8"));
    }

    private static String getDefaultCharSet() 
    {
        OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
        String enc = writer.getEncoding();
        return enc;
    }
} 