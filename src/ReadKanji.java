/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nhata
 */
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import javafx.beans.binding.Bindings;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class ReadKanji {
    public static void main(String args[]) throws SQLException{
        String sql = "insert into kanjin3(kanji,onyomi,kunyomi,english) values(?,?,?,?)";
        Connection con ;
        con = DriverManager.getConnection("jdbc:mysql://localhost/jlpt?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true", "root", "564488");
        PreparedStatement pr = con.prepareStatement(sql);
    
        try {
    PDDocument document = null;
    document = PDDocument.load(new File("C:\\Users\\nhata\\Downloads\\KanjiList.N3.pdf"));

    document.getClass();
    if (!document.isEncrypted()) {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        PDFTextStripper Tstripper = new PDFTextStripper();
        String st = Tstripper.getText(document);
        String[] s = new String[10000];
        int status =0;
        int j=0;
        int sum =0;
        st = st.replaceAll(".*JLPT.*", "");
        for(int i=0;i<st.length();i++){
            if (i == st.length() - 3)
                break;
            if(status == 0){
                if(st.charAt(i)=='\r' && st.charAt(i+1) =='\n' && isKanji(st.charAt(i+2)))
                {
                    j = i+2;
                    status = 1;
                    continue;
                }
            }
            else if(status == 1){
                if(st.charAt(i) =='\r' && st.charAt(i+1) =='\n' && (st.charAt(i+2) == ' ' || isKanji(st.charAt(i+2)))){
                    String nothing;
                    nothing = st.substring(j, i);
                    s[sum] =  nothing;
                    sum++;
                    status = 0;
                    if (isKanji(st.charAt(i+2))) {
                        j = i+2;
                        status = 1;
                        continue;
                    }
                }
            }
        }
        for(int a = 0;a<sum;a++){
            s[a] = s[a].replace("\r\n", "");
            ArrayList<String> extractWord = extractJapaneseWord(s[a]);
            ArrayList<String> onyomi = new ArrayList<String>();
            ArrayList<String> kunyomi = new ArrayList<String>();
            String english = "";
            String kanjiStr = "";
            String onyomiStr = "";
            String kunyomiStr = "";
            for(String string : extractWord) {
                if(string.length()<=0)
                    continue;
                if(isKanjiWord(string))
                     kanjiStr = string;
                else if(isKatakana(string.charAt(0)))
                     onyomi.add(string);
                else if(isHiragana(string.charAt(0)))
                     kunyomi.add(string);
                else if(isAlfabet(string.charAt(0))) {
                     english = string;
                     break;
                 }
                 else System.out.printf("UNKNOW __%s__",string);
            }
            pr.setString(1, kanjiStr);
            if (onyomi.size()>0) {
                onyomiStr = String.join("/", onyomi);
                pr.setString(2, onyomiStr);
            }
            else pr.setNull(2, java.sql.Types.CHAR);
            if (kunyomi.size()>0) {
                kunyomiStr = String.join("/", kunyomi);
                pr.setString(3, kunyomiStr);
            }
            else pr.setNull(3, java.sql.Types.CHAR);
            pr.setString(4, english);
            pr.executeUpdate();
            pr.clearParameters();
        }
    st = st.replace(" ", "_");
    st = st.replace("\r", "*");
    st = st.replace("\n", "X\n");

    System.out.print(st);
    pr.close();
    con.close();
         
    }
    } catch (Exception e) {
        e.printStackTrace();
   }
}
    public static boolean isHiragana(char c) {
        return (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HIRAGANA);
    }
    public static boolean isKatakana(char c) {
        return (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA);
    }
    public static boolean isKanji(char c) {
        return (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
    }
    public static boolean isJapaneseWord(char c) {
        return (isHiragana(c) || isKatakana(c) || isKanji(c));
    }
    public static boolean isAlfabet(char c) {
        return (('a'<= c && c <='z') || ('A'<= c && c <='Z'));
    }
    public static ArrayList<String> extractJapaneseWord(String s){
        ArrayList<String> result = new ArrayList<>();
        boolean read = false;
        String temp = ""; 
        for (int i = 0; i< s.length();i++) {
            if(!read && isJapaneseWord(s.charAt(i))) {
                read = true;
                temp = "";
                temp = temp + s.charAt(i);
                continue;
            }
            if (read && (isJapaneseWord(s.charAt(i)) || s.charAt(i) == '.' || s.charAt(i) == '-')) {
                temp = temp + s.charAt(i);
                continue;
            }
            if (read && !isJapaneseWord(s.charAt(i))) {
                result.add(temp);
                temp = "";
                read = false;
            }
        }
        result.add(getEnglish(s));
        return result;
    }
    public static boolean isKanaWord(String s) {
        for (int i = 0; i<s.length();i++) {
            if (isHiragana(s.charAt(i)) || isKatakana(s.charAt(i))) {
                continue;
            }
            else
                return false;
        }
        return true;
    }
    public static boolean isKanjiWord(String s) {
        for (int i = 0; i<s.length();i++) {
            if (isKanji(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    public static String getEnglish(String s) {
        int start = 0;
        for (int i = 0; i < s.length();i++) {
            if (isAlfabet(s.charAt(i))) {
                start = i;
                break;
            }
        }
        int end = start;
        while(!isJapaneseWord(s.charAt(end)) && end < s.length() - 1) {
            end ++;
        }
        while(end > 1) {
            if(s.charAt(end - 1)== ' ' || s.charAt(end - 1)== '\t' || s.charAt(end - 1)== '\n')
            {
                end--;
                continue;
            }
            else break;
        }
        return s.substring(start,end);
    }
}


