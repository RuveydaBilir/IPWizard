import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPWizard {
    private static final UserPref userPref = new UserPref();
    private static final String VOL = "1.0";

    private static String correctIP(String ipStr) throws Exception{
        String regex = "^\\d+\\.\\d+\\.\\d+\\.\\d+$";
        Pattern pattern = Pattern.compile(regex);
        String letterRegex = "[a-zA-Z]";
        String charRegex = "[^0-9.]";

        while(true){
            Matcher matcher = pattern.matcher(ipStr);
            boolean isIpPattern = matcher.matches();

            if(isIpPattern){
                return ipStr;
            }

            if(ipStr.contains(" ")){
                ipStr = ipStr.replaceAll(" ", "");
            }
            else if(ipStr.matches(".*" + charRegex + ".*")){
                ipStr = ipStr.replaceAll(charRegex, "");
            }
            else if(ipStr.matches(".*" + letterRegex + ".*")){
                ipStr = ipStr.replaceAll(letterRegex, "");
            }
            else{
                throw new Exception("Error: Wrong IP format.");
            }

        }
    }

    private static void banner(){
        String banner ="""
                        ___ ______        _____ _____   _    ____  ____  
                       |_ _|  _ \\ \\      / /_ _|__  /  / \\  |  _ \\|  _ \\ 
                        | || |_) \\ \\ /\\ / / | |  / /  / _ \\ | |_) | | | |
                        | ||  __/ \\ V  V /  | | / /_ / ___ \\|  _ <| |_| |
                       |___|_|     \\_/\\_/  |___/____/_/   \\_\\_| \\_\\____/ 

                       """
                       + "\t".repeat(5) + "vol: " + VOL +"\n";
        
        System.out.println(banner);
    }

    private static void calculate(IP ip) {
        double abuseDBSev = userPref.getAbuseDBSev();
        double vtSev = userPref.getvtSev();
        double cISPSev = userPref.getCountryISPSev();
        double dateSev = userPref.getDateSev();
        double relatedSev = userPref.getRelatedSev();
        double dateScore = 0;
        double cISPScore = 0;

        //CALCULATIONNNSSS!!!!!!!!!!!!!!!!!!!
        double totalSev = abuseDBSev + vtSev +cISPSev + dateSev + relatedSev;
    
        double abuseDBWeight = abuseDBSev / totalSev;
        double vtWeight = vtSev / totalSev;
        double cISPWeight = cISPSev / totalSev;
        double dateWeight = dateSev / totalSev;
        double relatedWeight = relatedSev / totalSev;

        int sumOfList = 0;
        for(int i=0; i<ip.getRelatedScoreList().size(); i++){
            sumOfList+=ip.getRelatedScoreList().get(i);
        }
    
        double abuseDBNormalizedScore = ip.getAbuseDBScore() / 100.0;
        double vtNormalizedScore = ip.getVTScore() / 92.0;
        double listNormalizedScore = sumOfList / 92.0;
        
        if (userPref.getResCountryList().contains(ip.getCountry().replace("\"", ""))) {
            ip.setIsFromResCount(true);
            cISPScore = 1;
        } else {
            ip.setIsFromResCount(false);
        }
        if (userPref.getResISPList().contains(ip.getISP().replace("\"", ""))) {
            ip.setIsFromResISP(true);
            cISPScore = 1;
        } else {
            ip.setIsFromResISP(false);
        }
        if(ip.isActive()){
            dateScore=1;
        }
        
        double score = (abuseDBNormalizedScore * abuseDBWeight 
                        + vtNormalizedScore * vtWeight 
                        + listNormalizedScore * relatedWeight 
                        + cISPScore * cISPWeight 
                        + dateScore * dateWeight) * 100;
        
        ip.updateScore(score);
    }
    
    public static void main(String[] args) throws Exception {
        ConfigManager cm = new ConfigManager();
        banner();
        //System.out.println("It can check every IP score you need.\n");
        Scanner scan = new Scanner(System.in);
        System.out.print(">> Enter the IPv4 address: ");
        String ipStr = scan.nextLine();
        IP ip = new IP(correctIP(ipStr));
        System.out.println("Checking IPv4 address " + ip.getIP());
        System.out.println();
        
        RequestResponse reqRes = new RequestResponse(ip);
        reqRes.sendGetRequestAbuseDB();
        reqRes.sendGetRequestVTforIP();
        calculate(ip);
        ip.print();
    }
}
