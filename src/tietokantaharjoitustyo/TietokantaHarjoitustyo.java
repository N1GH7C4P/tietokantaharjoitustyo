/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tietokantaharjoitustyo;

import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author kimmo
 */

public class TietokantaHarjoitustyo {

    private static Connection db;
    private static Statement s;
    private static Scanner lukija;
    private static PreparedStatement paikkaHakuNimiPS;
    private static PreparedStatement paikkaHakuIdPS;
    private static PreparedStatement pakettiHakuKoodiPS;
    private static PreparedStatement tapahtumaHakuIdPS;
    private static PreparedStatement tapahtumaMaaraHakuIdPS;
    private static PreparedStatement tapahtumaHakuPaikkaPS;
    private static PreparedStatement idHakuAsiakasPS;
    
    private static int luoTietokanta() {
        try {
            
            //LUODAAN YHTEYS TIETOKANTAAN
            db = DriverManager.getConnection("jdbc:sqlite:pakettienSeuranta.db");
            s = db.createStatement();
            
            //LUODAAN TIETOKANNAN VALMIIT TAULUT
            s.execute("CREATE TABLE Paikat (id INTEGER PRIMARY KEY, nimi TEXT UNIQUE)");
            s.execute("CREATE TABLE Paketit (id INTEGER PRIMARY KEY, asiakas_id INTEGER, seurantakoodi TEXT UNIQUE)");
            s.execute("CREATE TABLE Asiakkaat (id INTEGER PRIMARY KEY, nimi TEXT UNIQUE)");
            s.execute("CREATE TABLE Tapahtumat (id INTEGER PRIMARY KEY, kuvaus TEXT, paikka_id INTEGER, paketti_id INTEGER, date TEXT)");
            //s.execute("CREATE INDEX idx_tapahtumat ON Tapahtumat (paketti_id);");
            
            //LUODAAN VALMIIT STATEMENTIT
            paikkaHakuNimiPS = db.prepareStatement("SELECT id FROM Paikat WHERE nimi=?");
            paikkaHakuIdPS = db.prepareStatement("SELECT nimi FROM Paikat WHERE id=?");
            pakettiHakuKoodiPS = db.prepareStatement("SELECT id FROM Paketit WHERE seurantakoodi=?");
            tapahtumaHakuIdPS = db.prepareStatement("SELECT * FROM Tapahtumat WHERE paketti_id=?");
            tapahtumaHakuPaikkaPS = db.prepareStatement("SELECT * FROM Tapahtumat WHERE paikka_id=?");
            idHakuAsiakasPS = db.prepareStatement("SELECT id FROM Asiakkaat WHERE nimi=?");
            tapahtumaMaaraHakuIdPS = db.prepareStatement("SELECT COUNT(id) AS count FROM Tapahtumat WHERE paketti_id=?");
            
        }
        catch(SQLException e) {
            System.out.println("Jotain meni pieleen tietokannan luonnissa. "+e.getMessage());
            return 0;
        }
        return 1;
    }
    
    //LISÄÄ PAKETIN ASIAKKAAN ID:llä JA SEURANTAKOODILLA
    private static void lisaaPaketti(int asiakasId, String koodi, Statement s) {
        try{
            s.execute("INSERT INTO Paketit (seurantakoodi, asiakas_id) VALUES ('"+koodi+"','"+asiakasId+"')");
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan. Onko tietokanta luotu? Message: "+e.getMessage());
        }
    }
    
    //LISÄÄ PAKETILLE KUVAILLUN TAPAHTUMAN TIETYSSÄ PAIKASSA JA LISÄÄ SILLE AIKALEIMAN
    private static void lisaaTapahtuma(int pakettiId, int paikkaId, String kuvaus, Statement s) {
        try{
             s.execute("INSERT INTO Tapahtumat(paketti_id, paikka_id, kuvaus, date) VALUES ('"+pakettiId+"','"+paikkaId+"','"+kuvaus+"',datetime('now'))");
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan. Onko tietokanta luotu? Message: "+e.getMessage());
        }
    }
    
    //LISÄÄ NIMETYN PAIKAN
    private static void lisaaPaikka(String nimi, Statement s) {
        try {
            s.execute("INSERT INTO Paikat (nimi) VALUES ('"+nimi+"')");
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan. Onko tietokanta luotu? Message: "+e.getMessage());
        }
    }
    
    //LISÄÄ NIMETYN ASIAKKAAN
    private static void lisaaAsiakas(String nimi, Statement s) {
        try {
            s.execute("INSERT INTO Asiakkaat (nimi) VALUES ('"+nimi+"')");
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan. Onko tietokanta luotu? Message: "+e.getMessage());
        }
    }
    
    //PALAUTTAA ASIAKKAAN ID:n NIMEN PERUSTEELLA
    private static int haeAsiakasId(String nimi, PreparedStatement idHakuAsiakasPs) {
        try{
            idHakuAsiakasPs.setString(1,nimi);
            ResultSet r = idHakuAsiakasPs.executeQuery();
            if (r.next()) {
                int asiakasId = r.getInt("id");
                return asiakasId;
            }
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan. Onko tietokanta luotu? Message: "+e.getMessage());
        }
        return 0;
    }
    
    //PALAUTTAA PAIKAN ID:n NIMEN PERUSTEELLA
    private static int haePaikkaId(String paikka, PreparedStatement paikanHakuNimiPS) {
        try {
            paikanHakuNimiPS.setString(1,paikka);
            ResultSet r = paikanHakuNimiPS.executeQuery();
            int paikkaId = r.getInt("id");
            return paikkaId;
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan. Onko tietokanta luotu?  Message: "+e.getMessage());
        }
        return 0;
    }
    
    //PALAUTTAA PAIKAN NIMEN ID:n PERUSTEELLA
    private static String haePaikkaNimi(int id, PreparedStatement paikkaHakuIdPS) {
        try {
            paikkaHakuIdPS.setInt(1,id);
            ResultSet r = paikkaHakuIdPS.executeQuery();
            if(r.isClosed()) {
                return null;
            }
            String nimi = r.getString("nimi");
            return nimi;
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan: "+e.getMessage());
        }
        return null;
    }
    
    //PALAUTTAA PAKETIN ID:n NIMEN PERUSTEELLA
    private static int haePakettiId(String koodi, PreparedStatement pakettiHakuKoodiPS) {
        try {
            pakettiHakuKoodiPS.setString(1,koodi);
            ResultSet r = pakettiHakuKoodiPS.executeQuery();
            if (r.next()) {
                int pakettiId = r.getInt("id");
                return pakettiId;
            }
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan. Onko tietokanta luotu? Message: "+e.getMessage());
        }
        return 0;
    }
    
    //PALAUTTAA LISTAN PAKETIN TAPAHTUMISTA
    private static ArrayList<String> haePaketinTapahtumat(int id, PreparedStatement tapahtumaHakuIdPS, PreparedStatement paikkaHakuIdPS) {
        ArrayList<String> tapahtumat = new ArrayList();
        try{
            tapahtumaHakuIdPS.setInt(1,id);
            ResultSet r = tapahtumaHakuIdPS.executeQuery();
            while(r.next()) {
                int paikkaId = r.getInt("paikka_id");
                String date = r.getString("date");
                String kuvaus = r.getString("kuvaus");
                String paikka = haePaikkaNimi(paikkaId, paikkaHakuIdPS);
                String tapahtuma = (date+", "+paikka+", "+kuvaus);
                tapahtumat.add(tapahtuma);
            }
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan: "+e.getMessage());
        }
        return tapahtumat;
    }
    
    //PALAUTTAA PAKETIN TAPAHTUMIEN LUKUMÄÄRÄN
    private static int haePaketinTapahtumienMaara(int id, PreparedStatement tapahtumaMaaraHakuIdPS) {
        int tapahtumia = 0;
        try {
            tapahtumaMaaraHakuIdPS.setInt(1,id);
            ResultSet r = tapahtumaMaaraHakuIdPS.executeQuery();
            if(r.isClosed()) {
                return tapahtumia;
            }
            tapahtumia = r.getInt("count");
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan: "+e.getMessage());
        }
        return tapahtumia;
    }
    
    //HAKEE LISTAN ASIAKKAAN (ID) PAKETEISTA
    private static ArrayList<String> haeAsiakkaanPaketit(int id, PreparedStatement tapahtumanHakuIdPs, PreparedStatement paikanHakuIdPS) {
        ArrayList<String> paketit = new ArrayList();
        try{    
            PreparedStatement p = db.prepareStatement("SELECT * FROM Paketit WHERE asiakas_id=?");
            p.setInt(1,id);
            ResultSet r = p.executeQuery();
            
            while(r.next()) {
                int pakettiId = r.getInt("id");
                String koodi = r.getString("seurantakoodi");
                int tapahtumienMaara = haePaketinTapahtumat(pakettiId, tapahtumanHakuIdPs, paikanHakuIdPS ).size();
                String tapahtumat = "Ei tapahtumia";
                if(tapahtumienMaara == 1) {
                    tapahtumat = " tapahtuma.";
                }
                else if(tapahtumienMaara > 1) {
                    tapahtumat = " tapahtumaa.";
                }
                String paketti = koodi+", "+tapahtumienMaara+tapahtumat;
                paketit.add(paketti);
            }
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan. Onko tietokanta luotu? Message: "+e.getMessage());
        }
        return paketit;
    }
    
    //PALAUTTAA TAPAHTUMIEN MÄÄRÄN TIETYSSÄ PAIKASSA(nimi) TIETTYNÄ PÄIVÄNÄ
    private static int haeTapahtumienMaaraPaikassaTiettynaPaivana(String paikka, String paiva, PreparedStatement paikanHakuNimiPS, PreparedStatement tapahtumanHakuPaikkaPS) {
        int tapahtumia = 0;
        try{
            int paikkaId = haePaikkaId(paikka, paikanHakuNimiPS);
            tapahtumanHakuPaikkaPS.setInt(1,paikkaId);
            ResultSet r = tapahtumanHakuPaikkaPS.executeQuery();
            while (r.next()) {
                String date = r.getString("date");
                if(!date.startsWith(paiva)) {
                    continue;
                }
                tapahtumia++;
            }
        }
        catch(SQLException e) {
            System.out.println("Jotain meni vikaan. Onko tietokanta luotu? Message: "+e.getMessage());
        }
        return tapahtumia;
    }
    
    public static void main(String[] args) throws SQLException {
        
        //LUODAAN LUKIJA
        lukija = new Scanner(System.in);
        
        //RENDERÖIDÄÄN PÄÄVALIKKO
        System.out.println("Tervetuloa pakettienseurantaohjelmaan.");
        System.out.println("(1) Luo uusi tietokanta.");
        System.out.println("(2) Lisää uusi paikka tietokantaan. ");
        System.out.println("(3) Lisää uusi asiakas.");
        System.out.println("(4) Lisää uusi paketti.");
        System.out.println("(5) Lisää uusi tapahtuma.");
        System.out.println("(6) Hae kaikki paketin tapahtumat.");
        System.out.println("(7) Hae kaikki asiakkaan paketit.");
        System.out.println("(8) Hae annetusta paikasta tapahtumien määrä tiettynä päivänä.");
        System.out.println("(9) Suorita tietokannan tehokkuustesti.");
        System.out.println("(10) Lopeta.");
        
        //CORE LOOP
        int input;
        while(true) {
            
            System.out.println("Valitse toiminto (1-9) tai valitse 10 lopettaaksesi.");
            input = lukija.nextInt();
            lukija.nextLine();
            
            //UUDEN TIETOKANNAN LUOMINEN
            switch (input) {
                case 1:
                    if(luoTietokanta() == 1) {
                        System.out.println("Tietokanta luotu.");
                    }   break;
                case 2:
                    {
                        System.out.println("Anna paikan nimi: ");
                        String nimi = lukija.nextLine();
                        lisaaPaikka(nimi, s);
                        System.out.println(nimi+" lisätty tietokantaan.");
                        break;
                    }
                case 3:
                    {
                        System.out.println("Anna asiakkaan nimi: ");
                        String nimi = lukija.nextLine();
                        lisaaAsiakas(nimi, s);
                        System.out.println(nimi+" lisätty tietokantaan.");
                        break;
                    }
                case 4:
                    {
                        System.out.println("Anna asiakkaan nimi: ");
                        String nimi = lukija.nextLine();
                        int asiakasId = haeAsiakasId(nimi, idHakuAsiakasPS);
                        if(asiakasId == 0) {
                            System.out.println("Asiakasta ei löytynyt.");
                            continue;
                        }   System.out.println("Anna paketin seurantakoodi");
                        String koodi = lukija.nextLine();
                        lisaaPaketti(asiakasId, koodi, s);
                        System.out.println("Paketti lisätty.");
                        break;
                    }
                case 5:
                    {
                        //SEURANTAKOODI
                        System.out.println("Anna paketin seurantakoodi");
                        String koodi = lukija.nextLine();
                        int pakettiId = haePakettiId(koodi, pakettiHakuKoodiPS);
                        if(pakettiId == 0) {
                            System.out.println("Pakettia ei löytynyt.");
                            continue;
                        }       //PAIKKA
                        System.out.println("Anna paikan nimi");
                        String paikka = lukija.nextLine();
                        int paikkaId = haePaikkaId(paikka,paikkaHakuNimiPS);
                        if(paikkaId == 0) {
                            System.out.println("Paikkaa ei löytynyt.");
                        }       //KUVAUS
                        System.out.println("Anna tapahtuman kuvaus: ");
                        String kuvaus = lukija.nextLine();
                        //LISÄTÄÄN TAPAHTUMA
                        lisaaTapahtuma(pakettiId,paikkaId,kuvaus,s);
                        System.out.println("Tapahtuma lisätty.");
                        break;
                    }
                case 6:
                    {
                        System.out.println("Anna paketinseurantakoodi: ");
                        String koodi = lukija.nextLine();
                        int id = haePakettiId(koodi, pakettiHakuKoodiPS);
                        ArrayList<String> tapahtumat = haePaketinTapahtumat(id, tapahtumaHakuIdPS, paikkaHakuIdPS);
                        Iterator i = tapahtumat.iterator();
                        while(i.hasNext()) {
                            System.out.println(i.next());
                        }       break;
                    }
                case 7:
                    {
                        System.out.println("Anna asiakkaan nimi: ");
                        String nimi = lukija.nextLine();
                        int id = haeAsiakasId(nimi, idHakuAsiakasPS);
                        ArrayList<String> paketit = haeAsiakkaanPaketit(id, tapahtumaHakuIdPS, paikkaHakuIdPS);
                        Iterator i = paketit.iterator();
                        while(i.hasNext()) {
                            System.out.println(i.next());
                        }       break;
                    }
                case 8:
                    {
                        System.out.println("Anna paikka: ");
                        String paikka = lukija.nextLine();
                        System.out.println("Anna päivämäärä muodossa yyyy-MM-dd: ");
                        String paiva = lukija.nextLine();
                        int tapahtumat = haeTapahtumienMaaraPaikassaTiettynaPaivana(paikka, paiva, paikkaHakuNimiPS, tapahtumaHakuPaikkaPS);
                        System.out.println("tapahtumia: "+tapahtumat);
                        break;
                    }
                case 9:
                    Random r = new Random(715517);
                    long t1 = System.nanoTime();
                    s.execute("BEGIN;");
                    // T1
                    for(int i = 0; i<1000; i++) {
                        String p = "P"+i;
                        lisaaPaikka(p,s);
                    }   long t2 = System.nanoTime();
                    long dt1 = (t2-t1)/1000000;
                    System.out.println(dt1+" ms");
                    // T2
                    for(int i = 0; i<1000; i++) {
                        String a = "A"+i;
                        lisaaAsiakas(a,s);
                    }   long t3 = System.nanoTime();
                    long dt2 = (t3-t2)/1000000;
                    System.out.println(dt2+" ms");
                    // T3
                    for(int i = 0; i<1000; i++) {
                        int rand1 = r.nextInt(1000);
                        String S = "S"+i;
                        lisaaPaketti(rand1,S,s);
                    }   long t4 = System.nanoTime();
                    long dt3 = (t4-t3)/1000000;
                    System.out.println(dt3+" ms");
                    // T4
                    for(int i = 0; i<1000000; i++) {
                        int rand1 = r.nextInt(1000);
                        int rand2 = r.nextInt(1000);
                        lisaaTapahtuma(rand1,rand2,"K",s);
                    }   long t5 = System.nanoTime();
                    long dt4 = (t5-t4)/1000000;
                    System.out.println(dt4+" ms");
                    s.execute("COMMIT;");
                    // T5
                    for(int i = 0; i<1000; i++) {
                        int rand1 = r.nextInt(1000000000);
                        haeAsiakkaanPaketit(rand1, tapahtumaHakuIdPS, paikkaHakuIdPS).size();
                    }   long t6 = System.nanoTime();
                    long dt5 = (t6-t5)/1000000;
                    System.out.println(dt5+" ms");
                    // T6
                    for(int i = 0; i<1000; i++) {
                        int rand1 = r.nextInt(1000);
                        int tapahtumia = haePaketinTapahtumienMaara(rand1, tapahtumaMaaraHakuIdPS);
                        //System.out.println(tapahtumia+" tapahtumaa.");
                    }   long t7 = System.nanoTime();
                    long dt6 = (t7-t6)/1000000;
                    System.out.println(dt6+" ms");
                    break;
                case 10:
                    System.exit(0);
                default:
                    System.out.println("Tuntematon komento.");
                    break;
            }
        }
    }
}