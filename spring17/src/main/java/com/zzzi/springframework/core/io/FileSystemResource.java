package com.zzzi.springframework.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**@author zzzi
 * @date 2023/11/4 14:47
 * 第二种获取资源文件的方式
 */
public class FileSystemResource implements Resource{
    private final File file;
    private final String path;

    public File getFile() {
        return file;
    }

    public String getPath() {
        return path;
    }

    public FileSystemResource(File file) {
        this.file = file;
        this.path=file.getPath();
    }

    public FileSystemResource(String path) {
        this.file = new File(path);
        this.path = path;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
}
