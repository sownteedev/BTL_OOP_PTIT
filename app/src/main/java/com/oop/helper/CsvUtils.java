package com.oop.helper;

import com.oop.db.oop.CashItem;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {

    public static void exportToCsv(OutputStream outputStream, List<CashItem> cashItems) throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_16);
             CSVWriter writer = new CSVWriter(osw)) {
            // Write BOM to ensure Excel recognizes UTF-8 encoding
            osw.write('\uFEFF');
            osw.flush();

            String[] header = {"id", "desc", "amount", "type", "time", "yearKey", "monthKey", "weekKey"};
            writer.writeNext(header);

            for (CashItem item : cashItems) {
                String[] data = {
                        String.valueOf(item.getId()),
                        item.getDesc(),
                        String.valueOf(item.getAmount()),
                        item.getType(),
                        String.valueOf(item.getTime()),
                        item.getYearKey(),
                        item.getMonthKey(),
                        item.getWeekKey()
                };
                writer.writeNext(data);
            }
        }
    }

    public static List<CashItem> importFromCsv(InputStream inputStream) throws IOException {
        List<CashItem> cashItems = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_16);
             CSVReader reader = new CSVReader(isr)) {
            String[] nextLine;
            reader.readNext(); // Skip header
            while ((nextLine = reader.readNext()) != null) {
                CashItem item = new CashItem();
                item.setId(Long.parseLong(nextLine[0]));
                item.setDesc(nextLine[1]);
                item.setAmount(Double.parseDouble(nextLine[2]));
                item.setType(nextLine[3]);
                item.setTime(Long.parseLong(nextLine[4]));
                item.setYearKey(nextLine[5]);
                item.setMonthKey(nextLine[6]);
                item.setWeekKey(nextLine[7]);
                cashItems.add(item);
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return cashItems;
    }
}