package com.example.pokayokeeatonv2;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pokayokeeatonv2.Modelos.ModeloBD;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements config.DatosCuadroDialogo{
    EditText edUser, edPass;
    ArrayList<String> fechas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edUser = findViewById(R.id.edtUSER);
        edPass = findViewById(R.id.edtPass);
        edUser.requestFocus();
        crearUserDefault();

        try{
            LimpiarXFecha();
            checkEstado();
            revisarFecha();
        }
        catch(Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void checkEstado(){
        ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
        SQLiteDatabase BD = adminBD.getWritableDatabase();
        BD.execSQL("Delete From Etiqueta where Estado = 0");
        BD.close();
    }
    private Date nextWeek(String Fecha, int dias){
        SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = fecha.parse(Fecha);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, dias);
        return calendar.getTime();
    }
    private void LimpiarXFecha(){
        Date date = new Date();
        DateFormat fechaActual = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = fechaActual.format(date);
        //String currentDate = "08/07/2021";
        fechas = getDates();
        boolean estado = false;
        if(fechas != null){
            for(int i = 0; i < fechas.size(); i++){
                String _auxFechaInicio = fechas.get(i);
                for(int j = 0; j < 7 ; j++ ){
                    Date _auxweekAfter = nextWeek(_auxFechaInicio, j);
                    DateFormat _auxFechaAfter = new SimpleDateFormat("dd/MM/yyyy");
                    String _auxFechaComparacion = _auxFechaAfter.format(_auxweekAfter);
                    if(currentDate.equals(_auxFechaComparacion)){
                        estado = true;
                        break;
                    }
                    else{
                        estado = false;
                    }
                }
                if(!estado){
                    ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
                    SQLiteDatabase BD = adminBD.getWritableDatabase();
                    BD.execSQL("Delete from Etiqueta where fechaInsercion = '" + _auxFechaInicio + "'" );
                    BD.close();
                }
            }
        }
    }
    private ArrayList<String> getDates(){
        ArrayList<String> fechas = new ArrayList<>();
        fechas.clear();
        ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
        SQLiteDatabase BD = adminBD.getWritableDatabase();
        Cursor fila = BD.rawQuery("SELECT DISTINCT fechaInsercion from Etiqueta", null);
        if(fila.moveToFirst()){
            do{
                fechas.add(fila.getString(0));
            }
            while(fila.moveToNext());
        }
        BD.close();
        return fechas;
    }
    public void Log(View v){
        String usuario = "";
        String pass = "";
        String query = "";
        try{
            ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
            SQLiteDatabase BD = adminBD.getWritableDatabase();
            if(!(edUser.getText().toString() == "") || !(edPass.getText().toString() == "")){
                query = "Select * from usuarios where nombre = '" + edUser.getText().toString() + "' and Pass = '" + edPass.getText().toString() + "'";
            }
            else{
                query = "Select * from usuarios where nombre = '' and Pass = ''";
            }
            Cursor fila = BD.rawQuery(query, null);
            if(fila.moveToNext()){
                usuario = fila.getString(1);
                pass = fila.getString(2);
            }
            BD.close();
            if(!(usuario.isEmpty()) || !(pass.isEmpty())){
                Intent intent = new Intent(this, Packing.class);
                startActivity(intent);
            }
            else
                Toast.makeText(this, "Sin acceso", Toast.LENGTH_SHORT).show();
        }
        catch (Exception ex){
            Toast.makeText(this, "" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void config(View v){
        config con = new config();
        con.CuadroDialogo(this, MainActivity.this);
    }
    @Override
    public void resultadoCuadroDialogo(String user, String pass) {
        ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
        SQLiteDatabase BD = adminBD.getWritableDatabase();
        ContentValues add = new ContentValues();
        add.put("nombre", user);
        add.put("Pass", pass);
        BD.insert("usuarios", null, add);
        BD.close();
    }
    private void crearUserDefault(){
        ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
        SQLiteDatabase BD = adminBD.getWritableDatabase();
        ContentValues add = new ContentValues();
        Cursor fila = BD.rawQuery("Select * from usuarios", null);
        if(!fila.moveToFirst()){
            add.put("nombre", "Test");
            add.put("Pass", "Test");
            BD.insert("usuarios", null, add);
        }
        BD.close();
    }

    private void revisarFecha() throws ParseException {
        SharedPreferences preferencias = getSharedPreferences("Preferencias", MODE_PRIVATE);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SharedPreferences.Editor editor = preferencias.edit();
        if (preferencias.contains("fecha")) {
            String fecha_actual = preferencias.getString("fecha", "");
            Date f_fecha_actual = format.parse(fecha_actual);
            Date date = new Date(System.currentTimeMillis());
            Date n_f_fecha = format.parse(date.toString());
            int milisecondsByDay = 86400000;
            int dias = (int) ((n_f_fecha.getTime() - f_fecha_actual.getTime()) / milisecondsByDay);
            if (dias == 4) {
                ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
                BorrarReportesAtrasados(adminBD, fecha_actual, "Registros");
                BorrarReportesAtrasados(adminBD, fecha_actual, "Etiqueta");
                editor.putString("fecha", format.format(n_f_fecha));
                editor.commit();
            } else return;
        } else {
            editor.putString("fecha", format.format(new Date()));
        }
    }

    public void BorrarReportesAtrasados(ModeloBD adminBD, String fecha, String tabla){
        SQLiteDatabase BD = adminBD.getWritableDatabase();
        BD.execSQL("delete from " + tabla + " WHERE fechaInsercion = " + fecha);
        BD.close();
    }
}