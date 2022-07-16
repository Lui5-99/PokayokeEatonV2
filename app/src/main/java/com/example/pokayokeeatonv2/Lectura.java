package com.example.pokayokeeatonv2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pokayokeeatonv2.Modelos.Escaneos;
import com.example.pokayokeeatonv2.Modelos.ModeloBD;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import repack.org.bouncycastle.crypto.util.Pack;

public class Lectura extends AppCompatActivity {
    ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
    ArrayList<String> DatosRec;
    ArrayList<Escaneos> escaneos;
    EditText edCodigo2D, edAIAG, edLineSet;
    TextView tvCantidad;
    Button btSiguiente, btBorrar, btCancelar;
    int PiezasEscaneadas;
    boolean estado = false;
    int contador = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    String currentDate = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lectura);
        //Permisos para la lectura y escritura del almacenamiento de la memoria del dispositivo
        int PERMISSION = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(PERMISSION != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
        //Datos recibidos de la activity Packing
        Bundle extras = getIntent().getExtras();
        DatosRec = extras.getStringArrayList("datos");
        PiezasEscaneadas = extras.getInt("Piezas");
        //Si las piezas vienen null, se asignan a 0
        if(PiezasEscaneadas == 0){
            PiezasEscaneadas = 0;
        }
        else{
            PiezasEscaneadas = extras.getInt("Piezas");
        }
        //Declaración de los componentes para el uso de sus propiedades
        edCodigo2D = findViewById(R.id.edCodigo2D);
        edCodigo2D.requestFocus();
        //edCodigo2D.setInputType(InputType.TYPE_NULL);
        edAIAG = findViewById(R.id.edAIAG);
        edAIAG.setEnabled(false);
        edLineSet = findViewById(R.id.edLineSet);
        edLineSet.setEnabled(false);
        tvCantidad = findViewById(R.id.tvCantidad);
        btSiguiente = findViewById(R.id.btSiguiente);
        btSiguiente.setVisibility(View.INVISIBLE);
        edCodigo2D.setInputType(InputType.TYPE_NULL);
        btBorrar = findViewById(R.id.btLimpiar);
        btCancelar = findViewById(R.id.btCancelar);
        //Arreglo que contiene los archivos txt en la ruta de la aplicación
        String[] archivos = fileList();
        crearFolder("Transmisiones");
        validar();
        //Verifica si se escanean 2 o 3 etiquetas
        InputFilter[] filterArray = new InputFilter[1];
        switch (IsTwo(DatosRec)){
            case 1:
                tvCantidad.setText(" " + PiezasEscaneadas + " / " + DatosRec.get(2));
                edLineSet.setVisibility(View.INVISIBLE);
                filterArray[0] = new InputFilter.LengthFilter(8);
                edAIAG.setFilters(filterArray);
                /*TextDisable(12, edCodigo2D, "2D");
                TextDisable(8, edAIAG, "AIAG");*/
                break;
            case 2:
                tvCantidad.setText(" " + PiezasEscaneadas + " / " + DatosRec.get(2));
                edLineSet.setVisibility(View.VISIBLE);
                /*TextDisable(12, edCodigo2D,"2D");
                TextDisable(8, edAIAG, "AIAG");
                TextDisable(8, edLineSet, "LineSet");*/
                break;
            case 3:
                tvCantidad.setText(" " + PiezasEscaneadas + " / " + DatosRec.get(2));
                edAIAG.setVisibility(View.VISIBLE);
                edLineSet.setVisibility(View.INVISIBLE);
                edAIAG.setHint("LineSet");
                filterArray[0] = new InputFilter.LengthFilter(12);
                edAIAG.setFilters(filterArray);
                break;
        }

        //Verfica que no exista el archivo para crearlo
        if(archivoExist(archivos, currentDate + ".txt")){
            try {
                InputStreamReader archivo = new InputStreamReader(
                        openFileInput(currentDate + ".txt"));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                String todo = "";
                while (linea != null) {
                    todo = todo + linea + "\n";
                    linea = br.readLine();
                }
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        }

    }
    //metodo para detectar si el Codigo llega a su limite de caracteres
    /*private void TextDisable(int carc, EditText text, String metodo){
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Verificación para saber si el contador es igual a los caracteres del código
                contador += count;
                if(contador == carc){
                    next(metodo);
                    contador = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }*/
    @Override
    public void onBackPressed(){

    }
    //Método que detecta si se escanean 2 o 3 codigos o International
    private int IsTwo(ArrayList<String> datos){
        int estado = 0;
        int etiqueta = 0;
        ArrayList<String> Destinos = new ArrayList<>();
        ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
        SQLiteDatabase BD = adminBD.getWritableDatabase();
        Cursor fila = BD.rawQuery("Select NoEtiquetas from destinos where nombre = '" + datos.get(0) + "'", null);
        if(fila.moveToFirst()){
            etiqueta = fila.getInt(0);
            switch (etiqueta){
                //2 etiquetas = 1
                case 2: estado = 1;
                break;
                //3 = 2
                case 3: estado = 2;
                break;
                //4 = 2 diferentes
                case 4: estado = 3;
                break;
                default: estado = 0;
            }
        }
        BD.close();
        return estado;
    }
    //Método para simular un DataTable (Lista de objeto)
    private void llenarDataTable(){
        ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
        SQLiteDatabase BD = adminBD.getWritableDatabase();
        escaneos = new ArrayList<>();
        //Consuta a la base de datos
        Cursor fila = BD.rawQuery("SELECT DISTINCT IDRegistro, etiqueta2D, etiquetaAIAG, etiquetaLineSet, fecha  from Registros order by IDRegistro", null);
        //Verifica que tenga datos
        if(fila.moveToFirst()){
            do{
                //Llenadod del Datatable (Lista de objeto)
                escaneos.add(new Escaneos(fila.getString(1),fila.getString(2),fila.getString(3),fila.getString(4)));
            }
            //Se detiene cuando ya no encuentra datos
            while (fila.moveToNext());
        }
        BD.close();
    }
    //Método que almacena en memoria los datos escaneados y los guarda en un archivo de texto plano
    /*private void crearArchivoTxt(){
        llenarDataTable();
        try {
            //Se crea el archivo en memoria
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(currentDate + ".txt", Activity.MODE_PRIVATE));
            //Se escriben los datos escaneados en el archivo de te texto plano
            archivo.write("Work Order: " + DatosRec.get(1) + "\n");
            archivo.write("Gafete: " + DatosRec.get(3) + "\n");
            archivo.write("Cliente: " + DatosRec.get(4) + "\n");
            archivo.write("Destino: " + DatosRec.get(0) + "\n");
            archivo.write("Cantidad: " + DatosRec.get(2) + "\n");
            archivo.write("\n");
            //Verificación para el archivo, si tiene 2 etiquetas o 3
            if(IsTwo(DatosRec)){
                for(int i = 0; i < Integer.parseInt(DatosRec.get(2)); i++){
                    archivo.write("" + escaneos.get(i).getEtiqueta2D() + " | " + escaneos.get(i).getetiquetaAIAG() + " | " + escaneos.get(i).getfecha() +  "\n");
                }
            }
            else{
                for(int i = 0; i < Integer.parseInt(DatosRec.get(2)); i++){
                    archivo.write("" + escaneos.get(i).getEtiqueta2D() + " | " + escaneos.get(i).getetiquetaAIAG() + " | " + escaneos.get(i).getetiquetaLineSet() + " | " + escaneos.get(i).getfecha() + "\n");
                }
            }
            //Se guarda y cierra el archivo de texto
            archivo.flush();
            archivo.close();
            //Limpia el Datatable
            escaneos.clear();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    //Método que busca si el archivo que se creara existe
    private boolean archivoExist(String[] archivos, String name){
        //Busca en el arreglo de los archivos internos si existe
        for (int f = 0; f < archivos.length; f++)
            //Si lo encuentra devuelve verdadero
            if (name.equals(archivos[f]))
                return true;
        return false;
    }
    private void crearArchivo(String name){
        llenarDataTable();
        Document docPDF = new Document();
        try{
            File file = new File(Environment.getExternalStorageDirectory() + "/Documents/Transmisiones" , name + ".pdf");
            FileOutputStream ficheroPDF = new FileOutputStream(file.getAbsolutePath());
            PdfWriter writer = PdfWriter.getInstance(docPDF, ficheroPDF);
            docPDF.open();
            Bitmap logo = BitmapFactory.decodeResource(this.getResources(), R.drawable.eatonlogo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            logo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image image = Image.getInstance(stream.toByteArray());
            image.setAlignment(Element.ALIGN_LEFT);
            image.scaleAbsolute(125,25);
            docPDF.add(image);
            docPDF.add(new Paragraph("Work Order: " + DatosRec.get(1)));
            docPDF.add(new Paragraph("Gafete: " + DatosRec.get(3)));
            docPDF.add(new Paragraph("Cliente: " + DatosRec.get(4)));
            docPDF.add(new Paragraph("Destino: " + DatosRec.get(0)));
            docPDF.add(new Paragraph("Cantidad: " + DatosRec.get(2)));
            docPDF.add(new Paragraph(" "));
            if(IsTwo(DatosRec) == 1){
                for(int i = 0; i < Integer.parseInt(DatosRec.get(2)); i++){
                    docPDF.add(new Paragraph("" + escaneos.get(i).getEtiqueta2D() + " | " + escaneos.get(i).getetiquetaAIAG() + " | " + escaneos.get(i).getfecha()));
                }
            }
            else if(IsTwo(DatosRec) == 3){
                for(int i = 0; i < Integer.parseInt(DatosRec.get(2)); i++){
                    docPDF.add(new Paragraph("" + escaneos.get(i).getEtiqueta2D() + " | " + escaneos.get(i).getetiquetaLineSet() + " | " + escaneos.get(i).getfecha()));
                }
            }
            else{
                for(int i = 0; i < Integer.parseInt(DatosRec.get(2)); i++){
                    docPDF.add(new Paragraph("" + escaneos.get(i).getEtiqueta2D() + " | " + escaneos.get(i).getetiquetaAIAG() + " | " + escaneos.get(i).getetiquetaLineSet() + " | " + escaneos.get(i).getfecha()));
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            docPDF.close();
            Toast.makeText(this, "Archivo generado /Documents/Transmisiones", Toast.LENGTH_SHORT).show();
            escaneos.clear();
            Intent intent = new Intent(this, Packing.class);
            crearTXT();
            limpiarBD(false);
            startActivity(intent);
        }
    }
    //Metodo que crea la carpeta donde se guardaran los pdf
    private boolean crearFolder(String nombreCarpeta){
        //Se crea la carpeta
        File carpeta = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), nombreCarpeta);
        //Si no existe la crea y devuelve falso
        if(!carpeta.mkdirs()){
            return false;
        }
        else{
            return true;
        }
    }
    //Metodo que cambia a la siguiente etiqueta
    public void siguiente(){
        //aumenta las piezas escaneadas
        PiezasEscaneadas += 1;
        /*//actualiza los datos en la activity
        Intent intent = new Intent(this, Lectura.class);
        intent.putExtra("datos", DatosRec);
        intent.putExtra("Piezas", PiezasEscaneadas);
        //Guarda los escaneos en la BD*/
        Escaneos();
        btBorrar.setEnabled(true);
        btSiguiente.setEnabled(true);
        btCancelar.setEnabled(true);
        edCodigo2D.setText("");
        edAIAG.setText("");
        edLineSet.setText("");
        edAIAG.setEnabled(false);
        edLineSet.setEnabled(false);
        tvCantidad.setText(" " + PiezasEscaneadas + " / " + DatosRec.get(2));
        //startActivity(intent);
        edCodigo2D.requestFocus();
        /*btBorrar.setEnabled(true);
        btSiguiente.setEnabled(true);*/
        if(PiezasEscaneadas == Integer.parseInt(DatosRec.get(2))){
            /*alerta();*/
            ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
            SQLiteDatabase BD = adminBD.getWritableDatabase();
            Date date = new Date();
            DateFormat fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            currentDate = fecha.format(date);
            //crearArchivoTxt();
            crearArchivo(currentDate);
            limpiarBD(false);
            BD.execSQL("update Etiqueta set Estado = 1 where CodigoBarras = " + Integer.parseInt(DatosRec.get(3)));
            regresar();
        }
    }
    private boolean isExistCodigo2D(String _2D){
        boolean bFound = false;
        ModeloBD adminDB = new ModeloBD(this, "Eaton", null, 1);
        SQLiteDatabase BD = adminBD.getWritableDatabase();
        Cursor fila = BD.rawQuery("SELECT * FROM Etiquetas where CodigoBarras = '" + _2D + "1", null);
        if(fila.moveToFirst()){
            bFound = true;
        }
        return bFound;
    }

    //Metodo que guarda los escaneos en la BD
    private void Escaneos(){
        //Se lee el codigo 2D y se le retira el prefijo DM17
        String Codigo2D = edCodigo2D.getText().toString();
        String _auxCod[] = Codigo2D.split("DM17");
        Codigo2D = _auxCod[1];
        ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
        SQLiteDatabase BD = adminBD.getWritableDatabase();
        ContentValues add = new ContentValues();
        ContentValues add2 = new ContentValues();
        Date date = new Date();
        DateFormat fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat fechaoHrs = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = fecha.format(date);
        String currentDateoHrs = fechaoHrs.format(date);
        String _aux = "";
        /*boolean estado = isExistCodigo2D(Codigo2D);
        if(!estado) {*/
            //Verifcia si son 2 o 3 etiquetas para la inserción a la BD
            if (IsTwo(DatosRec) == 1) {
                add.put("CodigoBarras", DatosRec.get(3));
                add.put("etiqueta2D", Codigo2D);
                add.put("etiquetaAIAG", edAIAG.getText().toString());
                add.put("etiquetaLineSet", "");
                add.put("fecha", currentDate);
                add.put("fechaInsercion", currentDateoHrs);
                add.put("Estado", 0);
                BD.insert("Etiqueta", null, add);

                add2.put("CodigoBarras", DatosRec.get(3));
                add2.put("etiqueta2D", Codigo2D);
                add2.put("etiquetaAIAG", edAIAG.getText().toString());
                add2.put("etiquetaLineSet", "");
                add2.put("fecha", currentDate);
                add2.put("fechaInsercion", currentDateoHrs);
                BD.insert("Registros", null, add2);
            } else if (IsTwo(DatosRec) == 3) {
                String _auxCodigoAIAG = edAIAG.getText().toString().trim();
                _auxCodigoAIAG = _auxCodigoAIAG.length() == 12 ? _auxCodigoAIAG.substring(4, 12) : _auxCodigoAIAG;
                add.put("CodigoBarras", DatosRec.get(3));
                add.put("etiqueta2D", Codigo2D);
                add.put("etiquetaAIAG", "");
                add.put("etiquetaLineSet", _auxCodigoAIAG);
                add.put("fecha", currentDate);
                add.put("fechaInsercion", currentDateoHrs);
                add.put("Estado", 0);
                BD.insert("Etiqueta", null, add);

                add2.put("CodigoBarras", DatosRec.get(3));
                add2.put("etiqueta2D", Codigo2D);
                add2.put("etiquetaAIAG", "");
                add2.put("etiquetaLineSet", _auxCodigoAIAG);
                add2.put("fecha", currentDate);
                add2.put("fechaInsercion", currentDateoHrs);
                BD.insert("Registros", null, add2);
            } else {
                add.put("CodigoBarras", DatosRec.get(3));
                add.put("etiqueta2D", Codigo2D);
                add.put("etiquetaAIAG", edAIAG.getText().toString());
                add.put("etiquetaLineSet", edLineSet.getText().toString());
                add.put("fecha", currentDate);
                add.put("fechaInsercion", currentDateoHrs);
                add.put("Estado", 0);
                BD.insert("Etiqueta", null, add);

                add2.put("CodigoBarras", DatosRec.get(3));
                add2.put("etiqueta2D", Codigo2D);
                add2.put("etiquetaAIAG", edAIAG.getText().toString());
                add2.put("etiquetaLineSet", edLineSet.getText().toString());
                add2.put("fecha", currentDate);
                add2.put("fechaInsercion", currentDateoHrs);
                BD.insert("Registros", null, add2);
            }
        /*}
        else{
            Toast.makeText(this, "Etiqueta previamente escaneada", Toast.LENGTH_SHORT).show();
        }*/
        add.clear();
        add2.clear();
        BD.close();
    }
    //Metodo que limpia la tabla registros
    private void limpiarBD(boolean estado){
        try{
            ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
            SQLiteDatabase BD = adminBD.getWritableDatabase();
            //BD.execSQL("delete from Registros where CodigoBarras = '" + DatosRec.get(3) + "'" );
            if(estado)
                //BD.execSQL("delete from Etiqueta where CodigoBarras = '" + DatosRec.get(3) + "'" );
            BD.close();
        }
        catch(Exception ex){
            Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Contacta al desarrollador", Toast.LENGTH_SHORT).show();
        }
    }

    //Método para regresar de Activity
    private void regresar(){
        Intent intent = new Intent(this, Packing.class);
        startActivity(intent);
    }
    //Método para validar si se presiona enter en cada uno de los EditText
    public void validar(){
        edCodigo2D.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    next("2D");
                    btBorrar.setEnabled(false);
                    btSiguiente.setEnabled(false);
                    btCancelar.setEnabled(false);
                }
                return false;
            }
        });
        edAIAG.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    next("AIAG");
                    btBorrar.setEnabled(false);
                    btSiguiente.setEnabled(false);
                    btCancelar.setEnabled(false);
                }
                return false;
            }
        });
        edLineSet.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    next("LineSet");
                    btBorrar.setEnabled(false);
                    btSiguiente.setEnabled(false);
                    btCancelar.setEnabled(false);
                }
                return false;
            }
        });
    }
    //Metodo que valida si ya se escaneo el codigo 2D
    private boolean validarCodigo2D(){
        boolean estado = false;
        try{
            String Codigo2D = edCodigo2D.getText().toString();
            String _auxCod[] = Codigo2D.split("DM17");
            Codigo2D = _auxCod[1];
            String C2D = "";
            ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
            SQLiteDatabase BD = adminBD.getWritableDatabase();
            Cursor fila = BD.rawQuery("Select etiqueta2D from Etiqueta where etiqueta2D = '" + Codigo2D + "'", null);
            //Si existe devuelve Verdadero
            if(fila.moveToFirst()){
                estado = true;
            }
            BD.close();
            return estado;
        }
        catch (Exception ex){
            return estado;
        }
    }
    //Metodo que valida que el codigo 2D Contiene su prefijo correspondiente
    private boolean prefijo(String codigo){
        try{
            String[] _auxCodigo = codigo.split("DM17");
            boolean bandera = false;
            //Si existe devuelve verdadero
            if(_auxCodigo.length > 1){
                bandera = true;
            }
            else if(_auxCodigo == null){
                bandera = false;
            }
            return bandera;
        }
        catch(Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    //Método que valida el EditText y cambia al siguiente
    private void next(String metodo){
        //Verificiación para el tipo de codigo
        try{
            switch (metodo){
                case "2D":
                    //Verifica que no este vacio
                    if(!edCodigo2D.getText().toString().equals("")){
                        //Verificca que contenga el prefijo
                        if(prefijo(edCodigo2D.getText().toString())){
                            //Verifica que no exista y si no existe habilita el Código AIAG
                            if(validarCodigo2D()){
                                Toast.makeText(Lectura.this, "Etiqueta previamente Escaneada", Toast.LENGTH_SHORT).show();
                                limpiar();
                            }
                            else{
                                //edCodigo2D.setEnabled(false);
                                edAIAG.setEnabled(true);
                                //edAIAG.requestFocus();
                                edAIAG.setInputType(InputType.TYPE_NULL);
                            }
                        }
                        else{
                            Toast.makeText(Lectura.this, "No se encontró el prefijo", Toast.LENGTH_SHORT).show();
                            limpiar();
                        }
                    }
                    else{
                        edAIAG.setEnabled(false);
                    }
                    break;
                case "AIAG":
                    String Codigo[] = edCodigo2D.getText().toString().split("DM17");
                    String _auxCodigo  = Codigo[1];
                    String _auxCodigoAIAG = edAIAG.getText().toString();
                    //Verifica que los codigos hagan match
                    if(IsTwo(DatosRec) == 3){
                        if(edAIAG.getText().toString().length() == 12)
                            _auxCodigoAIAG = _auxCodigoAIAG.substring(4, 12);
                        else
                            _auxCodigoAIAG += "";
                    }
                    if(_auxCodigoAIAG.equals(_auxCodigo)){
                        //Si son 3 etiquetas habilita la siguiente y si no acaba con ese escaneo
                        if(IsTwo(DatosRec) == 2){
                            //edAIAG.setEnabled(false);
                            edLineSet.setEnabled(true);
                            //edLineSet.requestFocus();
                            edLineSet.setInputType(InputType.TYPE_NULL);
                            estado = true;
                            edCodigo2D.requestFocus();
                        }
                        else if(IsTwo(DatosRec) == 1 || IsTwo(DatosRec) == 3){
                            siguiente();
                        }
                    }
                    else{
                        Toast.makeText(this, "No coinciden los códigos", Toast.LENGTH_SHORT).show();
                        edLineSet.setEnabled(false);
                        estado = false;
                        limpiar();
                    }
                    break;
                case "LineSet":
                    String codigo = edCodigo2D.getText().toString().trim();
                    codigo = codigo.replace("DM17", "");
                    String codigoLineSet = edLineSet.getText().toString().trim();
                    boolean isExistC = codigoLineSet.contains(codigo);
                    //Verifica que los codigos hagan match
                    if(isExistC && estado){
                        siguiente();
                    }
                    else{
                        Toast.makeText(this, "No coinciden los códigos", Toast.LENGTH_SHORT).show();
                        limpiar();
                    }
                    break;
            }
        }
        catch(Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    //Método para cerrar la activity
    public void cerrar(View v){
        limpiarBD(true);
        Intent intent = new Intent(this, Packing.class);
        startActivity(intent);
    }
    //Método que limpia la activity
    public void limpiar(){
        btBorrar.setEnabled(false);
        btSiguiente.setEnabled(false);
        edCodigo2D.setText("");
        edAIAG.setText("");
        edLineSet.setText("");
        edCodigo2D.setInputType(InputType.TYPE_NULL);
        edAIAG.setInputType(InputType.TYPE_NULL);
        edAIAG.setEnabled(false);
        edLineSet.setInputType(InputType.TYPE_NULL);
        edLineSet.setEnabled(false);
        edCodigo2D.requestFocus();
    }
    public void limpiarClick(View v){
        limpiar();
    }
    private void crearTXT(){
        Date date = new Date();
        DateFormat fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = fecha.format(date);
        try {

            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput( DatosRec.get(4) + currentDate + ".txt", Activity.MODE_PRIVATE));
            //Se escriben los datos escaneados en el archivo de te texto plano
            archivo.write("Work Order: " + DatosRec.get(1));
            archivo.write("Gafete: " + DatosRec.get(3));
            archivo.write("Cliente: " + DatosRec.get(4));
            archivo.write("Destino: " + DatosRec.get(0));
            archivo.write("Cantidad: " + DatosRec.get(2));
            archivo.flush();
            archivo.close();
            //borrarTablasBD(adminBD, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void borrarTablasBD (ModeloBD adminBD, boolean lim) {
        if (lim) {
            SQLiteDatabase BD = adminBD.getWritableDatabase();
            BD.execSQL("delete from usuarios");
            BD.execSQL("delete from clientes");
            BD.execSQL("delete from destinos");
            BD.execSQL("delete from Etiqueta");
            BD.execSQL("delete from Registros");
            BD.close();
        }
    }
}