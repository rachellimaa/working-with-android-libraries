package com.example.retrofit;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TOKEN = "";
    private Button botaoEnviar;
    private RespostaServidor resposta = new RespostaServidor();
    private ProgressDialog progress;
    private RetrofitService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = ServiceGenerator.createService(RetrofitService.class);

        botaoEnviar = findViewById(R.id.button_enviar);
        listenersButtons();
    }

    private void listenersButtons() {
        botaoEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress = new ProgressDialog(MainActivity.this);
                progress.setTitle("enviando...");
                progress.show();

                //chama o retrofit para fazer a requisição no webservice
                retrofitGetURL();
            }
        });
    }

    private void setNovosParametros() {
        int mNumeroCasas = resposta.getNumero_casas();
        String mTextoCifrado = resposta.getCifrado();
        StringBuilder mTextoDecifrado = decodeString(mTextoCifrado, mNumeroCasas);

        try {
            String mResumoCriptografico = gerarSHA1(mTextoDecifrado);

            //      Log.d("XX", "show JSON: token= " + TOKEN + " numero_casas:= " + mNumeroCasas +
            //            " cifrado:= " + mTextoCifrado + " decifrado:= " + mTextoDecifrado + " resumo criptografico:= " + mResumoCriptografico);

            String json;

            json = criarJSON(mNumeroCasas, mTextoCifrado, mTextoDecifrado, mResumoCriptografico);

            postFile(json);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String criarJSON(int numCasas, String cifrado, StringBuilder decifrado, String resumo) {
        RespostaServidor respostaServidor = new RespostaServidor();

        respostaServidor.setToken(TOKEN);
        respostaServidor.setNumero_casas(numCasas);
        respostaServidor.setCifrado(cifrado);
        respostaServidor.setDecifrado(String.valueOf(decifrado));
        respostaServidor.setResumo_criptografico(resumo);

        Gson gson = new Gson();
        String json = gson.toJson(respostaServidor);

            Log.d("XX ", "json " + json);

        return json;
    }

    private void postFile(String json) {

        File file = new File("raw/answer.json");

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), json);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("answer", file.getName(), requestFile);

        Call<ResponseBody> call = service.postFile(TOKEN, body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    ResponseBody responseBody = response.body();
                    Log.d("XX", "resposta final " + responseBody.toString());
                } else {
                    ResponseBody responseBody = response.errorBody();
                    Log.d("XX", "error " + responseBody.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Erro no Servidor ", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String gerarSHA1(StringBuilder mTextoDecifrado) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        byte[] result = messageDigest.digest(String.valueOf(mTextoDecifrado).getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private StringBuilder decodeString(String textoCifrado, int numeroCasas) {
        String myString = textoCifrado.toLowerCase();

        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < myString.length(); i++) {
            char c = myString.charAt(i);

            if (Character.isLetter(c)) {
                if (c == 97) {
                    c = 119;
                } else {
                    c = (char) (c - numeroCasas);
                }
            }

            strBuilder.append(c);
        }

        //   Log.d("XX", "criptografada " + strBuilder);

        return strBuilder;
    }

    private void retrofitGetURL() {
        Call<RespostaServidor> call = service.getURL(TOKEN);

        call.enqueue(new Callback<RespostaServidor>() {
            @Override
            public void onResponse(Call<RespostaServidor> call, Response<RespostaServidor> response) {
                if (response.isSuccessful()) {

                    RespostaServidor respostaServidor = response.body();

                    if (respostaServidor != null) {
                        resposta.setCifrado(respostaServidor.getCifrado());
                        resposta.setNumero_casas(respostaServidor.getNumero_casas());
                        resposta.setToken(respostaServidor.getToken());
                        resposta.setResumo_criptografico(respostaServidor.getResumo_criptografico());

                        setNovosParametros();
                    } else {
                        Toast.makeText(getApplicationContext(), "Resposta Nula do Servidor ", Toast.LENGTH_LONG).show();
                        ResponseBody responseBody = response.errorBody();
                        Log.d("XX", "error " + responseBody);
                    }
                    progress.dismiss();
                }
            }

            @Override
            public void onFailure(Call<RespostaServidor> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Erro no Servidor ", Toast.LENGTH_LONG).show();
            }
        });

    }
}
