package com.medvault.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;

// here Tika will read my PDF/Word/docx/txt file and give me a plain text
@Service
public class TikaService {
    private final Tika tika = new Tika();

    // Extracts text from a file path (pdf/docx/txt etc)
    public String extractText(Path path) throws Exception{
        return tika.parseToString(new File(path.toString()));
    }
}
