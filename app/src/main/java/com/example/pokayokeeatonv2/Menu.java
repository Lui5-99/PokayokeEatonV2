package com.example.pokayokeeatonv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import com.example.pokayokeeatonv2.Modelos.ModeloBD;

public class Menu extends AppCompatActivity {

    ModeloBD adminBD = new ModeloBD(this, "Eaton", null, 1);
    boolean var;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //var = limiteRegistrosAlcanzado(adminBD);
        //borrarTablasBD(adminBD, var);
    }
    public void Escaner(View v){
        Intent intent = new Intent(this, Packing.class);
        startActivity(intent);
    }
    public void Archivos(View v){
        Intent intent = new Intent(this, Archivos.class);
        startActivity(intent);
    }

    public boolean limiteRegistrosAlcanzado (ModeloBD adminBD) {
        boolean estado = true;
        int lim = 100;
        int counts = 0;
        SQLiteDatabase BD = adminBD.getWritableDatabase();
        Cursor fila = BD.rawQuery("Select COUNT(IDEtiqueta) FROM Etiqueta", null);
        if (fila.moveToFirst()) {
            counts = fila.getInt(0);
        }
        BD.close();
        return counts > lim ? true : false;
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