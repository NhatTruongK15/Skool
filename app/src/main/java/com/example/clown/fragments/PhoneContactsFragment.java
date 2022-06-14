package com.example.clown.fragments;

import static com.example.clown.activities.ContactsActivity.FRAGMENT_FRIENDS_POS;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class PhoneContactsFragment extends Fragment implements SuggestedUserAdapter.ISuggestedUserListener {
    private static final String TAG = PhoneContactsFragment.class.getName();

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

    protected final ActivityResultLauncher<String> mActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), bIsGranted -> {
                if (bIsGranted) {
                    Log.e(TAG, "Permission's granted!");
                    getPhoneContacts(requireActivity().getApplicationContext());

                    filterPhoneContactsAppUsers();
                } else {
                    Log.e(TAG, "Permission's denied!");
                    ((ContactsActivity) requireActivity()).getContactViewPager().setCurrentItem(FRAGMENT_FRIENDS_POS, true);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPhoneContactsBinding.inflate(inflater, container, false);

        Init();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivityResultLauncher.launch(Manifest.permission.READ_CONTACTS);
        mSuggestedUsersList.clear();
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

        mPhoneContactsAdapter = new SuggestedUserAdapter(getContext(), mSuggestedUsersList, this);
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
                colIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
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

    @Override
    public void onSuggestedUserClicked(User suggestedUser) { // requester nguoi nhan
        String senderID = mCurrentUser.getID();
        String receiverID = suggestedUser.getID();

        suggestedUser.getReceivedRequests().add(senderID);
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(suggestedUser.getID())
                .update(Constants.KEY_RECEIVED_REQUESTS, suggestedUser.getReceivedRequests());

        mCurrentUser.getSentRequests().add(receiverID);
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getID())
                .update(Constants.KEY_SENT_REQUESTS, mCurrentUser.getSentRequests());

        removeRequest();
    }

    private void removeRequest() {
        Log.e(TAG, "Requests removed!");

        List<User> oldList = new ArrayList<>(mSuggestedUsersList);

        for (User friend : oldList)
            //noinspection SuspiciousMethodCalls
            if (!mSuggestedUsersList.contains(friend.getID())) {
                int i = oldList.indexOf(friend);
                mSuggestedUsersList.remove(i);
                mPhoneContactsAdapter.notifyItemRemoved(i);
            }
    }
}