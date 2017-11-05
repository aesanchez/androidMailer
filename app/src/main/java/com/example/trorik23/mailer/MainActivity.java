package com.example.trorik23.mailer;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {
    EditText mEmail, mSubject, mParagraph;
    Button mButtonSend;
    Toast statusToast;

    ProgressDialog sendingEmailDialog;

    String mailerAddress = MailCredentials.ADDRESS;
    String mailerPassword = MailCredentials.PASSWORD;
    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonSend = (Button) findViewById(R.id.button);
        mEmail = (EditText) findViewById(R.id.email);
        mSubject = (EditText) findViewById(R.id.subject);
        mParagraph = (EditText) findViewById(R.id.paragraph);

        setListeners();
    }

    private void setListeners(){
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                CharSequence toastText = null;
                if(mEmail.getText().toString().equals("")){
                    toastText = "Completar email";
                }else if(!isValidEmail(mEmail.getText())){
                    toastText = "Formato de email invalido";
                }else if(mSubject.getText().toString().equals("")){
                    toastText = "Completar asunto";
                }else if(mParagraph.getText().toString().equals("")){
                    toastText = "Completar texto";
                }else{
                    if(!isNetworkAvailable()){
                        toastText = "No hay coneccion a internet";
                    }else{
                        sendingEmailDialog = new ProgressDialog(MainActivity.this);
                        sendingEmailDialog.setMessage("Enviando mail...");
                        sendingEmailDialog.setCancelable(false);
                        sendingEmailDialog.setIndeterminate(false);
                        sendingEmailDialog.show();
                        new MailSenderTask().execute(mEmail.getText().toString(), mSubject.getText().toString(), mParagraph.getText().toString());
                    }
                }
                if(toastText != null){
                    statusToast.cancel();
                    statusToast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
                    statusToast.show();
                }
            }
        });
    }

    private boolean isValidEmail(CharSequence target) {
        if(target == null){
            return false;
        }else{
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class MailSenderTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... args) {
            boolean result = false;
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Properties properties = new Properties();
            properties.put("mail.smtp.host","smtp.googlemail.com");
            properties.put("mail.smtp.socketFactory.port","465");
            properties.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.auth","true");
            properties.put("mail.smtp.port","465");
            try{
                session = Session.getDefaultInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailerAddress, mailerPassword);
                    }
                });
                if(session != null){
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(mailerAddress));
                    message.setSubject("From AndroidApp");
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailerAddress));
                    String content = "From:<br>" + args[0] + "<br><br>";
                    content += "Subject:<br>" + args[1] + "<br><br>";
                    content += "Text:<br>" + args[2];
                    message.setContent(content,"text/html; charset=utf-8");

                    Transport.send(message);
                    result = true;
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Context context = getApplicationContext();
            CharSequence toastText;
            sendingEmailDialog.dismiss();
            if(result){
                toastText = "Mail enviado";
                mEmail.setText("");
                mSubject.setText("");
                mParagraph.setText("");
            }
            else
                toastText = "Error al enviar mail";
            statusToast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
            statusToast.show();
        }
    }
}


