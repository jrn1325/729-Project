/**
 * Copyright 2015-2016 by Metanome Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.metanome.backend.input.file;

import au.com.bytecode.opencsv.CSVReader;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

/**
 * {@link FileIterator}s are Iterators over lines in a file file.
 *
 * @author Jakob Zwiener
 */
public class FileIterator implements RelationalInput {
    protected List<String> nextLine = new ArrayList<>();
    protected String fileName;
    protected int numberOfKeys = 0;
    // Initialized to -1 because of lookahead
    protected int currentLineNumber = -1;
    protected FileReader fr = null;
    protected BufferedReader br = null;
    protected JSONParser parser = null;
    protected JSONObject json = null;
    //protected String line = null;
    private ArrayList<String> keyList = new ArrayList<>();
    protected ConfigurationSettingFileInput setting = null;


    public FileIterator(String fileName, Reader fr, ConfigurationSettingFileInput setting) throws InputIterationException {
        this.fileName = fileName;
        this.setting = setting;

        try {
            // Initialize file and buffered readers
            this.fr = new FileReader(setting.getFileName());
            this.br = new BufferedReader(fr);

            this.nextLine = readNextLine();


            // Call readNextLine and get the number of keys in the json object
            //this.numberOfKeys = keys.size();
            //this.nextLine = readNextLine();
            if (this.nextLine != null) {
                this.numberOfKeys = this.nextLine.size();
            }


        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Error reading file '" + this.fileName + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasNext() {
        return !(this.nextLine == null);
    }

    @Override
    public List<String> next() throws InputIterationException {
        List<String> currentLine = this.nextLine;

        if (currentLine == null) {
            return null;
        }
        this.nextLine = readNextLine();

        return currentLine;
    }

    protected List<String> readNextLine() throws InputIterationException {
        String line;
        try {
            // Read a line of json file
            line = this.br.readLine();

            // Check if the line is null
            if (line == null) {
                return null;
            }
            currentLineNumber++;

            // Initialize a json parser
            parser = new JSONParser();

            // Turn the line into a json object
            json = (JSONObject)parser.parse(line);
        } catch (IOException | ParseException ioe) {
            throw new InputIterationException("Could not read next line in file input", ioe);
        }

        // Get all the keys from json object
        Set<String> keys = json.keySet();

        // Create a list for values
        List<String> list = new ArrayList<String>();
        keyList.clear();

        // Loop through all the fields of the json object and store keys and values in their respective lists
        for(Object obj: keys) {
            String key = (String)obj;
            String value = json.get(key).toString();
            keyList.add(key);
            list.add(value);
        }

        // Return list of values
        return Collections.unmodifiableList(list);
    }

    @Override
    public void close() throws IOException {
        br.close();
        fr.close();
    }
    @Override
    public int numberOfColumns() {
        return numberOfKeys;
    }

    @Override
    public String relationName() {
        return fileName;
    }

    @Override
    public List<String> columnNames() {
        return keyList;
    }
}