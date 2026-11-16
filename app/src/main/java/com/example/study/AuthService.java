package com.example.study;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthService {

    private final FirebaseAuth auth;

    public AuthService() {
        auth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> register(
            String name,
            String email,
            String password) {

        return auth.createUserWithEmailAndPassword(email, password)
                .onSuccessTask(result -> {

                    FirebaseUser user = result.getUser();

                    if (user == null) {
                        return Tasks.forException(
                                new Exception("User not found")
                        );
                    }

                    UserProfileChangeRequest profile =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                    return user.updateProfile(profile)
                            .continueWith(task -> {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                return result;
                            });
                });
    }

    public Task<AuthResult> login(
            String email,
            String password) {

        return auth.signInWithEmailAndPassword(email, password);
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void logout() {
        auth.signOut();
    }
}
