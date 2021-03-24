package com.example.ftptest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Livre {
    protected int ID;
    protected int audioOuPas;
    protected String nomAuteur;
    protected String nomLivre;
    protected int date;
    protected int nombreDL;
    protected String[] taille;
    protected String[] genres;
    protected String resume;
    protected String urlDL;
    protected String ftpURLim;
    protected String ftpURLepub;
    protected String ftpURLpdf;
    protected Bitmap bitm;



    public Livre(String sc) {
        String[] data = sc.split(";"); // on recupere les infos dans un tableau de String
        this.ID = Integer.parseInt(data[0]);
        this.audioOuPas = Integer.parseInt(data[1]);
        this.nomAuteur = data[2].replaceAll("_", " ");
        this.nomLivre = data[3].replaceAll("_", " ");
        this.date = Integer.parseInt(data[4]);
        this.nombreDL = Integer.parseInt(data[5]);
        this.taille = data[6].split("/");
        this.genres = data[7].split("/");
        this.resume = data[8].replaceAll("~", "\n");

        this.ftpURLim = turnInDownloadable(data[2]).replaceAll(" ","_") + "/" + turnInDownloadable(data[3]).replaceAll(" ","_") + "/";

        if (this.audioOuPas == 0) { // livre ecrit pur
            this.urlDL = "";
            this.ftpURLepub = turnInDownloadable(data[2]).replaceAll(" ","_") + "/" + turnInDownloadable(data[3]).replaceAll(" ","_") + "/";
            this.ftpURLpdf = turnInDownloadable(data[2]).replaceAll(" ","_")+ "/" + turnInDownloadable(data[3]).replaceAll(" ","_") + "/";
        }
        else if (this.audioOuPas == 1) { // audio pur
            this.urlDL = "";
            for (int i = 9; i < data.length; i++) {
                if (i == 9) {
                    this.urlDL += data[i];
                } else {
                    this.urlDL += ";" + data[i];
                }
            }
            this.ftpURLepub = "";
            this.ftpURLpdf = "";
        }
        else {    // les deux
            this.urlDL = "";
            this.ftpURLepub = turnInDownloadable(data[2]).replaceAll(" ","_") + "/" + turnInDownloadable(data[3]).replaceAll(" ","_") + "/";
            this.ftpURLpdf = turnInDownloadable(data[2]).replaceAll(" ","_") + "/" + turnInDownloadable(data[3]).replaceAll(" ","_") + "/";
            for (int i = 9; i < data.length; i++) {
                if (i == 9) {
                    this.urlDL += data[i];
                } else {
                    this.urlDL += ";" + data[i];
                }
            }
        }
    }

    public String getTaillePdf() {
        return taille[1];
    }
    public String getTailleEpub() {
        return taille[0];
    }

    public int getID() {
        return ID;
    }

    public int getAudioOuPas() {
        return audioOuPas;
    }

    public int getDate() {
        return date;
    }

    public String[] getGenres() {
        return genres;
    }

    public String getNomAuteur() {
        return nomAuteur;
    }

    public String getNomLivre() {
        return nomLivre;
    }

    public String getResume() {
        return resume;
    }

    public int getNombreDL() {
        return nombreDL;
    }

    public String getFtpURLepub() {
        return ftpURLepub;
    }

    public String getFtpURLim() {
        return ftpURLim;
    }

    public String getFtpURLpdf() {
        return ftpURLpdf;
    }

    public String getUrlDL() {
        return urlDL;
    }

    public void setBitm(Bitmap bitm) {
        this.bitm = bitm;
    }

    public Bitmap getBitm() {
        return bitm;
    }



    @Override
    public String toString() { // cree une ligne qui peut etre utilisé pour recréer le livre
        String to_return = ID +
                ";" + audioOuPas +
                ";" + nomAuteur +
                ";" + nomLivre +
                ";" + date +
                ";" + nombreDL +
                ";" + taille[0] +
                "/" + taille[1] +
                ";" ;

        for (int i = 0; i < genres.length; i++) {
            if (i == 0) {
                to_return += genres[i];
            } else {
                to_return += "/" + genres[i];
            }
        }
        to_return +=
                ";" + resume +
                        ";" + urlDL + ";";
        return to_return;
    }




    // recupere l'image
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Bitmap getBitmap(Context ctx, FTPClient ftpClient){
        String filePath = ctx.getFilesDir().getPath().toString() + "/temp/" ;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.createDirectory(Paths.get(filePath));
            }
            else {
                File folder = new File(filePath);
                folder.mkdir();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // test si o est connecté au serveur
        boolean test = ftpClient.isConnected();
        test = ftpClient.isAvailable();

        try {
            // pour recup un fichier en binaire (pas de modif quel que soit le type)
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.login("pi", "m0t2pa55e");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);


        } catch (IOException e) {
            e.printStackTrace();
        }

        filePath+= this.ID + ".jpg" ;
        File myObj = new File(filePath);

        try {
            if (myObj.createNewFile()) {
                downloadImage(turnInDownloadable(this.getNomLivre()).replaceAll(" ","_")+".jpg", filePath, ftpClient,this.getFtpURLim());
            }

            if(myObj.exists()) {
                Bitmap bit = BitmapFactory.decodeFile(myObj.getAbsolutePath());
                this.bitm = bit;
                myObj.delete();
                return bit;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void bitmapVoid(){
        this.bitm = null;
    } // enleve l'image de la memoire


    // telecharge l'image
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean downloadImage(String filename, String filePath, FTPClient ftpClient, String pathServer){


        try (OutputStream os = new FileOutputStream(filePath)) {

            // Download file from FTP server.
            boolean status = ftpClient.retrieveFile(pathServer+filename, os);
            String test = String.valueOf(status);


        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    // telecharge l'image du ftp sans la metre comme bitmap et renvoie l'addresse de l'image
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String launchDownload(Context ctx, FTPClient ftpClient){
        String filePath = ctx.getFilesDir().getPath().toString() + "/temp/" ;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.createDirectory(Paths.get(filePath));
            }
            else{
                File folder = new File(filePath);
                folder.mkdir();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean test = ftpClient.isConnected();
        test = ftpClient.isAvailable();
        try {
            ftpClient.login("pi", "m0t2pa55e");


        } catch (IOException e) {
            e.printStackTrace();
        }

        filePath+= this.ID + ".jpg" ;

        File myObj = new File(filePath);
        try {
            if (myObj.createNewFile()) {
                downloadImage(turnInDownloadable(this.getNomLivre()).replaceAll(" ","_")+".jpg", filePath, ftpClient,this.getFtpURLim());
                return myObj.getAbsolutePath();
            }

            else{
                return myObj.getAbsolutePath();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // regarde si l'image est deja telechargée
    public boolean isDownloaded(){
        if(bitm != null){
            return true;
        }
        else{
            return false;
        }

    }

    // enleve tous les caract qui ne passent pas dans les nom de dossier/fichier + les mets en lower case
    public String turnInDownloadableLowerCase(String toTurn){
        String turned = toTurn
                .replaceAll("é","e").replaceAll("è","e").replaceAll("ê","e").replaceAll("ë","e")
                .replaceAll("É", "E").replaceAll("È", "E").replaceAll("Ê", "E").replaceAll("Ë", "E")
                .replaceAll("à","a").replaceAll("â","a").replaceAll("ä","a")
                .replaceAll("À","A").replaceAll("Â","A")
                .replaceAll("î","i").replaceAll("ï","i")
                .replaceAll("Î","I").replaceAll("Ï","I")
                .replaceAll("ô","o").replaceAll("ö","o")
                .replaceAll("Ô","O").replaceAll("Ö","O")
                .replaceAll("ù","u").replaceAll("û","u").replaceAll("ü","u")
                .replaceAll("Ù","U").replaceAll("Û","U").replaceAll("Ü","U")
                .replaceAll("ÿ","y")
                .replaceAll("Ÿ","Y")
                .replaceAll("ç","c")
                .replaceAll("Ç","C").toLowerCase();
        return turned;

    }

    // enleve tous les caract qui ne passent pas dans les nom de dossier/fichier
    public String turnInDownloadable(String toTurn){
        String turned = toTurn
                .replaceAll("é","e").replaceAll("è","e").replaceAll("ê","e").replaceAll("ë","e")
                .replaceAll("É", "E").replaceAll("È", "E").replaceAll("Ê", "E").replaceAll("Ë", "E")
                .replaceAll("à","a").replaceAll("â","a").replaceAll("ä","a")
                .replaceAll("À","A").replaceAll("Â","A")
                .replaceAll("î","i").replaceAll("ï","i")
                .replaceAll("Î","I").replaceAll("Ï","I")
                .replaceAll("ô","o").replaceAll("ö","o")
                .replaceAll("Ô","O").replaceAll("Ö","O")
                .replaceAll("ù","u").replaceAll("û","u").replaceAll("ü","u")
                .replaceAll("Ù","U").replaceAll("Û","U").replaceAll("Ü","U")
                .replaceAll("ÿ","y")
                .replaceAll("Ÿ","Y")
                .replaceAll("ç","c")
                .replaceAll("Ç","C");
        return turned;

    }

    // pr mettre dans l'ordre alphabetique auteur
    public int compareAuteur(Livre book1){
        int length;

        if(getNomAuteur().toLowerCase() == book1.getNomAuteur().toLowerCase()){
            return 0;
        }
        if(getNomAuteur().length() < book1.getNomAuteur().length()){
            length = getNomAuteur().length();
        }
        else{
            length = book1.getNomAuteur().length();
        }
        for(int i = 0 ; i < length ; i++){
            if(getNomAuteur().toLowerCase().charAt(i) < book1.getNomAuteur().toLowerCase().charAt(i)){
                return 1;
            }
            else if(getNomAuteur().charAt(i) > book1.getNomAuteur().charAt(i)){
                return -1;
            }
        }
        if(length == getNomAuteur().length()){
            return 1;
        }
        else if(length == book1.getNomAuteur().length()){
            return -1;
        }
        return 0;
    }

    // pr mettre dans l'ordre alphabetique titres
    public int compareTitre(Livre book1){
        int length;
        if(getNomLivre().length() < book1.getNomLivre().length()){
            length = getNomLivre().length();
        }
        else{
            length = book1.getNomLivre().length();
        }
        for(int i = 0 ; i < length ; i++){
            if((getNomLivre()).charAt(i) < (book1.getNomLivre()).charAt(i)){
                return 1;
            }
            else if((getNomLivre()).charAt(i) > (book1.getNomLivre()).charAt(i)){
                return -1;
            }
        }
        if(length == getNomLivre().length()){
            return 1;
        }
        else if(length == book1.getNomLivre().length()){
            return -1;
        }
        return 0;
    }

    // pr mettre dans l'ordre date de parution
    public int compareDate(Livre book1){
        int length;
        if(getDate() < book1.getDate()){
            return 1;
        }
        else if(getDate() > book1.getDate()){
            return -1;
        }
        return 0;
    }

}
