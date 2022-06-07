package com.example.clown.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.clown.activities.ContactsActivity;
import com.example.clown.adapter.SuggestedUserAdapter;
import com.example.clown.databinding.FragmentPhoneContactsBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhoneContactsFragment extends Fragment {
    public static final String TAG = PhoneContactsFragment.class.getName();

    private FragmentPhoneContactsBinding binding;

    private SuggestedUserAdapter mPhoneContactsAdapter;
    private List<String> mPhoneContactsList;
    private List<User> mSuggestedUsersList;
    private User mCurrentUser;

    private final OnCompleteListener<QuerySnapshot> mOnCompleted = (task) -> {
        if (task.isSuccessful() && !task.getResult().isEmpty())
            for (DocumentSnapshot docSnap : task.getResult().getDocuments()) {
                User user = docSnap.toObject(User.class);
                mSuggestedUsersList.add(user);
            }

        setUpRecyclerView();
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPhoneContactsBinding.inflate(inflater, container, false);

        Init();

        getPhoneContacts(requireActivity().getApplicationContext());

        filterPhoneContactsAppUsers();

        return binding.getRoot();
    }

    private void filterPhoneContactsAppUsers() {
        if (mPhoneContactsList.isEmpty()) return;

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(Constants.KEY_PHONE_NUMBER, mPhoneContactsList)
                .get()
                .addOnCompleteListener(mOnCompleted);
    }

    private void Init() {
        mCurrentUser = (Objects.requireNonNull((ContactsActivity) getActivity())).getCurrentUser();
        mPhoneContactsList = new ArrayList<>();
        mSuggestedUsersList = new ArrayList<>();
    }

    private void setUpRecyclerView() {
        Log.e(TAG, "Set up phone contacts RecyclerView");
        mPhoneContactsAdapter = new SuggestedUserAdapter(getContext(), mSuggestedUsersList);
        mPhoneContactsAdapter.setCurrentUser(mCurrentUser);
        binding.phoneContactsRecyclerView.setAdapter(mPhoneContactsAdapter);
    }

    public void getPhoneContacts(Context context) {
        Log.e(TAG, "Getting Phone Contacts");

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        int colIndex;
        String id;
        String phoneNumber;

        if ((cursor != null ? cursor.getCount() : 0) > 0) {
            while (cursor.moveToNext()) {
                colIndex= cursor.getColumnIndex(ContactsContract.Contacts._ID);
                id = cursor.getString(colIndex);
                colIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

                if (cursor.getInt(colIndex) > 0) {
                    Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                    while (cursorInfo.moveToNext()) {
                        colIndex = cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        phoneNumber = cursorInfo.getString(colIndex).replaceAll("[()\\s-]+", "");
                        mPhoneContactsList.add(phoneNumber);
                    }
                    cursorInfo.close();
                }
            }
            cursor.close();
        }
    }
}