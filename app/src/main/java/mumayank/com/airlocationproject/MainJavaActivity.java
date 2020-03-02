package mumayank.com.airlocationproject;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mumayank.com.airlocationlibrary.AirLocation;

public class MainJavaActivity extends AppCompatActivity {

    private AirLocation airLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_java);

        airLocation = new AirLocation(this, true, true, new AirLocation.Callbacks() {
            @Override
            public void onSuccess(@NotNull Location location) {
                // do something
            }

            @Override
            public void onFailed(@NotNull AirLocation.LocationFailedEnum locationFailedEnum) {
                // do something
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        airLocation.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
