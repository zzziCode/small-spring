package com.zzzi.springframework.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
/**@author zzzi
 * @date 2023/11/1 14:40
 * 在这里实现第二种获取资源的方式
 */
public class FileSystemResource implements Resource{
    private final String path;
    private final File file;

    public FileSystemResource(String path) {
        this.path = path;
        this.file = new File(path);
    }
    public FileSystemResource(File file) {
        this.path = file.getPath();
        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }
}
