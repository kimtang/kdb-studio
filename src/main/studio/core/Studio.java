package studio.core;

import java.awt.Font;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.UIManager;
import studio.kdb.Config;
import studio.kdb.Server;
import studio.ui.StudioPanel;

public class Studio {
    public static String unescapeJavaString(String st) {

    StringBuilder sb = new StringBuilder(st.length());

    for (int i = 0; i < st.length(); i++) {
        char ch = st.charAt(i);
        if (ch == '\\') {
            char nextChar = (i == st.length() - 1) ? '\\' : st
                    .charAt(i + 1);
            // Octal escape?
            if (nextChar >= '0' && nextChar <= '7') {
                String code = "" + nextChar;
                i++;
                if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                        && st.charAt(i + 1) <= '7') {
                    code += st.charAt(i + 1);
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                    }
                }
                sb.append((char) Integer.parseInt(code, 8));
                continue;
            }
            switch (nextChar) {
            case '\\':
                ch = '\\';
                break;
            case 'b':
                ch = '\b';
                break;
            case 'f':
                ch = '\f';
                break;
            case 'n':
                ch = '\n';
                break;
            case 'r':
                ch = '\r';
                break;
            case 't':
                ch = '\t';
                break;
            case '\"':
                ch = '\"';
                break;
            case '\'':
                ch = '\'';
                break;
            // Hex Unicode: u????
            case 'u':
                if (i >= st.length() - 5) {
                    ch = 'u';
                    break;
                }
                int code = Integer.parseInt(
                        "" + st.charAt(i + 2) + st.charAt(i + 3)
                                + st.charAt(i + 4) + st.charAt(i + 5), 16);
                sb.append(Character.toChars(code));
                i += 5;
                continue;
            }
            i++;
        }
        sb.append(ch);
    }
    return sb.toString();
    }
    public static void main(final String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        if (System.getProperty("os.name", "").contains("OS X")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            //     System.setProperty("apple.awt.brushMetalLook", "true");
            System.setProperty("apple.awt.showGrowBox", "true");
            System
                .setProperty("com.apple.mrj.application.apple.menu.about.name", "Studio for kdb+");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
            System.setProperty("com.apple.macos.smallTabs", "true");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }

        if (Config.getInstance().getLookAndFeel() != null) {
            try {
                UIManager.setLookAndFeel(Config.getInstance().getLookAndFeel());
            } catch (Exception ex) {
                // go on with default one
                ex.printStackTrace();
            }
        }

        UIManager.put("Table.font", new javax.swing.plaf.FontUIResource("Monospaced", Font.PLAIN,
            UIManager.getFont("Table.font").getSize()));
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        StudioPanel studio = StudioPanel.init(args);

        try {
            while (true) {
                Scanner scanner = new Scanner(System.in);
                String line = scanner.nextLine();
                if( line.startsWith(":") ){

                    if(line.startsWith(":addSetServer")){
                        String newLine = line.substring(15,line.length() - 3);                        
                        String pattern = "/\\s(.+):(.+):(\\d*):(.*):(.*)";
                        Pattern r = Pattern.compile(pattern);
                        Matcher m = r.matcher(newLine);

                        if (m.find( )) {
                            Server ss = new Server();
                            ss.setName(m.group(1));
                            ss.setHost(m.group(2));
                            ss.setPort(Integer.parseInt(m.group(3)));
                            ss.setUsername(m.group(4));
                            ss.setPassword(m.group(5));
                            ss.setAuthenticationMechanism("Username and password");
                            studio.setServer(ss);
                            System.out.printf(" change to:%s%n",m.group(1)+":"+m.group(2)+":"+m.group(3));
                        } else {
                           System.out.printf(" %s%n",newLine);
                           System.out.printf(" %s%n","NO MATCH");
                        }
                    }

                    if(line.startsWith(":executeK4Query")){
                        String newLine = unescapeJavaString(line.substring(17,line.length() - 5));
                        System.out.printf("%s%n",newLine);
                        studio.executeK4Query(newLine);
                    }

                    if(line.startsWith(":exportAsExcel")){
                        String newLine = unescapeJavaString(line.substring(":exportAsExcel".length() + 2,line.length() - 2));
                        System.out.printf(" exportAsExcel: %s%n",newLine);
                        studio.exportAsExcel(newLine);
                    }

                    if(line.startsWith(":exportAsTxt")){
                        String newLine = unescapeJavaString(line.substring(":exportAsTxt".length() + 2,line.length() - 2));
                        System.out.printf(" exportAsTxt: %s%n",newLine);
                        studio.exportAsTxt(newLine);
                    }

                    if(line.startsWith(":exportAsCSV")){
                        String newLine = unescapeJavaString(line.substring(":exportAsCSV".length() + 2,line.length() - 2));
                        System.out.printf(" exportAsCSV: %s%n",newLine);
                        studio.exportAsCSV(newLine);
                    }

                    if(line.startsWith(":chart")){
                        System.out.printf(" chart:%n");                        
                        studio.chart();
                    }                                        

                }
                else{studio.executeK4Query(line);}
                
            }
        } catch(java.lang.IllegalStateException | java.util.NoSuchElementException e) {
            // System.in has been closed
            System.out.println("System.in was closed; exiting");
        }

    }
}
