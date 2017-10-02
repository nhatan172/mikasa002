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

public class ReadGammar{
    public static void main(String args[]) throws SQLException{
        String sql = "insert into vocabn3(kanji,hiragana,english) values(?,?,?)";
        Connection con ;
        con = DriverManager.getConnection("jdbc:mysql://localhost/jlpt?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true", "root", "564488");
        PreparedStatement pr = con.prepareStatement(sql);
    
        try {
    PDDocument document = null;
    document = PDDocument.load(new File("C:\\Users\\nhata\\Downloads\\VocabList.N3.pdf"));

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
        boolean force_find = false;
        int tempI = 0;
        boolean isKanjiTemp = false;
        while ( tempI < st.length() -2 ) {
            if (isKanji(st.charAt(tempI))&& st.charAt(tempI+1) == '\r' && st.charAt(tempI+2) == '\n' && (isHiragana(st.charAt(tempI+3))||isKanji(st.charAt(tempI))) ) {
                StringBuilder sb = new StringBuilder(st);
                sb.deleteCharAt(tempI+2);
                sb.deleteCharAt(tempI+1);
                st = sb.toString();
                tempI+=3;
                continue;
            }
            if (isHiragana(st.charAt(tempI))&& st.charAt(tempI+1) == '\r' && st.charAt(tempI+2) == '\n' && isJapaneseWord(st.charAt(tempI+3)) ) {
                StringBuilder sb = new StringBuilder(st);
                sb.deleteCharAt(tempI+2);
                sb.deleteCharAt(tempI+1);
                st = sb.toString();
                tempI+=3;
                continue;
            }
            if (isKatakana(st.charAt(tempI)) && st.charAt(tempI+1)== '\r' && st.charAt(tempI+2) == '\n' && isJapaneseWord(st.charAt(tempI+3)) ) {
                StringBuilder sb = new StringBuilder(st);
                sb.deleteCharAt(tempI+2);
                sb.deleteCharAt(tempI+1);
                st = sb.toString();
                tempI+=3;
                continue;
            }
            tempI++;
        }
        
        for(int i=0;i<st.length();i++){
            if (i == st.length() - 3)
                break;
            if(status == 0){
                if(st.charAt(i)=='\r' && st.charAt(i+1) =='\n' && isJapaneseWord(st.charAt(i+2)))
                {
                    j = i+2;
                    status = 1;
                    continue;
                }
            }
            else if(status == 1){
                if(st.charAt(i) == '/') {
                    force_find = true;
                    continue;
                }
                if(isKanji(st.charAt(i)) && !isKanjiTemp)
                    isKanjiTemp = true;
                if(st.charAt(i) ==' ' && st.charAt(i+1) =='\r' && st.charAt(i+2) =='\n' && isKanjiTemp) {
                    isKanjiTemp = false;
                    i += 2;
                    continue;
                }
                if( isKanjiTemp && isAlfabet(st.charAt(i)))
                    isKanjiTemp = false;
                if(st.charAt(i) == ' ' && isJapaneseWord(st.charAt(i+1)) && isKanjiTemp)
                    isKanjiTemp = false;
                
                if(force_find && isJapaneseWord(st.charAt(i)))
                    force_find = false;
                if(st.charAt(i) =='\r' && st.charAt(i+1) =='\n' && !isAlfabet(st.charAt(i+2))){
                    if (force_find)
                        continue;
                    String nothing;
                    nothing = st.substring(j, i);
                    s[sum] =  nothing;
                    isKanjiTemp = false;
                    sum++;
                    status = 0;
                    if (isJapaneseWord(st.charAt(i+2))) {
                        j = i+2;
                        status = 1;
                        continue;
                    }
                }
            }
        }
        for(int a = 0;a<sum;a++){
            s[a] = s[a].replace("\r\n", "");
//            System.out.println(Integer.toString(a)+"."+s[a]);
            ArrayList<String> extractWord = extractJapaneseWord(s[a]);
            ArrayList<String> kanji = new ArrayList<String>();
            ArrayList<String> kana = new ArrayList<String>();
            String english = "";
            String kanjiStr = "";
            String kanaStr = "";
            for(String string : extractWord) {
                 if(isKanjiWord(string))
                     kanji.add(string);
                 else if(isKanaWord(string))
                     kana.add(string);
                 else if(isAlfabet(string.charAt(0))) {
                     english = string;
                     break;
                 }
                 else System.out.printf("UNKNOW __%s__",string);
            }
            if (kanji.size()>0) {
                kanjiStr = String.join("/", kanji);
                pr.setString(1, kanjiStr);
            }
            else pr.setNull(1, java.sql.Types.CHAR);
            if (kana.size()>0) {
                kanaStr = String.join("/", kana);
                pr.setString(2, kanaStr);
            }
            else pr.setNull(2, java.sql.Types.CHAR);
            pr.setString(3, english);
            pr.executeUpdate();
            pr.clearParameters();
        }

//    st = st.replace(" ", "_");
//    st = st.replace("\r", "*");
//    st = st.replace("\n", "X\n");
//
//    System.out.print(st);
  
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
            if (read && isJapaneseWord(s.charAt(i))) {
                temp = temp + s.charAt(i);
                continue;
            }
            if (read && !isJapaneseWord(s.charAt(i))) {
                result.add(temp);
                temp = "";
                read = false;
            }
            if (!read && isAlfabet(s.charAt(i))) {
                result.add(s.substring(i,s.length()));
                break;
            }
        }
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
}


