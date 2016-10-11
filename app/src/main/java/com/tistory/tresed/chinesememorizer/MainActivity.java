package com.tistory.tresed.chinesememorizer;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static Button selectedButton;
    static int contentIndex;
    static boolean meaningMode;
    static List<Integer> shuffle;
    WordList wordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedButton = new Button(this);
        SharedPreferences prefs = getSharedPreferences("data", MODE_PRIVATE);
        contentIndex = prefs.getInt("lesson", 0);
        meaningMode = prefs.getBoolean("meaningMode", false);

        wordList = new WordList();

        load(true);

    }

    void load(boolean randomize) {
        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridLayout);
        Display display = getWindowManager().getDefaultDisplay();

        if(randomize) {
            shuffle = new ArrayList<>();
            for (int i = 0; i < wordList.letter[contentIndex].length; i++) shuffle.add(i);
            Collections.shuffle(shuffle);
        }
        gridLayout.removeAllViews();

        for (int i = 0; i < wordList.letter[contentIndex].length; i++) {
            int shuffledIndex = shuffle.get(i);
            Button button = new Button(this);
            String text = meaningMode?wordList.mean[contentIndex][shuffledIndex]:wordList.letter[contentIndex][shuffledIndex];
            button.setText(text);
            button.setWidth(display.getWidth() / 3);
            int textSize = meaningMode?15:32;
            if(!meaningMode) {
                switch (wordList.letter[contentIndex][shuffledIndex].length()) {
                    case 1:case 2:case 3:break;
                    case 4:
                        textSize = 24;
                        break;
                    case 5:
                        textSize = 19;
                        break;
                    case 6:
                        textSize = 16;
                        break;
                    case 7:
                        textSize = 14;
                        break;
                    default:
                        textSize = 10;
                        break;
                }
            }
            button.setTextSize(textSize);
            button.setHeight(200);
            button.setOnClickListener(this);
            gridLayout.addView(button);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // 메뉴버튼이 처음 눌러졌을 때 실행되는 콜백메서드
        // 메뉴버튼을 눌렀을 때 보여줄 menu 에 대해서 정의
        MenuItem item = (MenuItem)findViewById(R.id.menu_allwords);
        item.setTitle(wordList.isAllwords?"단어시험용":"All Words");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_info:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("APP INFO");
                alert.setMessage("THE CHINESE - 중국어의 길 1\n단어를 쉽게 외울 수 있도록 제작\n\n통계16 이병우\nsdr04055@snu.ac.kr");
                alert.setIcon(R.drawable.icon);
                alert.show();
                break;
            case R.id.menu_select:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this

                // 여기서 부터는 알림창의 속성 설정
                builder.setTitle("과를 선택하세요")        // 제목 설정
                        .setSingleChoiceItems(wordList.contentName, contentIndex, new DialogInterface.OnClickListener() {
                            // 목록 클릭시 설정
                            public void onClick(DialogInterface dialog, int index) {
                                contentIndex = index;
                                SharedPreferences prefs = getSharedPreferences("data", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putInt("lesson", contentIndex);
                                editor.commit();
                                selectedButton = null;
                                load(true);
                                dialog.cancel();
                            }
                        });

                AlertDialog dialog = builder.create();    // 알림창 객체 생성
                dialog.show();    // 알림창 띄우기
                break;
            case R.id.menu_switch:
                meaningMode = !meaningMode;
                SharedPreferences prefs = getSharedPreferences("data", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("meaningMode", meaningMode);
                editor.commit();
                load(true);
                break;
            case R.id.menu_reset:
                shuffle = new ArrayList<>();
                for (int i = 0; i < wordList.letter[contentIndex].length; i++)shuffle.add(i);
                load(false);
                break;
            case R.id.menu_allwords:
                //TODO 새 메뉴. 각 과의 모든 단어.
                wordList = wordList.isAllwords?new WordList():new WordList(true);
                load(true);
                break;
            //TODO 삭제 기능.
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Button thisButton = (Button) v;
        int index = getIndex(thisButton.getText().toString());
        selectedButton = thisButton;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this

        // 여기서 부터는 알림창의 속성 설정
        String title = meaningMode?wordList.letter[contentIndex][shuffle.get(index)]:wordList.mean[contentIndex][shuffle.get(index)];
        builder.setTitle(title)        // 제목 설정
                .setMessage(wordList.pronounce[contentIndex][shuffle.get(index)])        // 메세지 설정
                .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton("완료", new DialogInterface.OnClickListener() {
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ViewGroup layout = (ViewGroup) selectedButton.getParent();
                        if (null != layout) //for safety only  as you are doing onClick
                            layout.removeView(selectedButton);
                        dialog.cancel();

                    }
                })
                .setNegativeButton("모름", new DialogInterface.OnClickListener() {
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();    // 알림창 띄우기
    }

    public int getIndex(String s) {
        if(!meaningMode) {
            for (int i = 0; i < wordList.letter[contentIndex].length; i++) {
                if (s.equals(wordList.letter[contentIndex][shuffle.get(i)])) return i;
            }
        }
        else{
            for (int i = 0; i < wordList.mean[contentIndex].length; i++) {
                if (s.equals(wordList.mean[contentIndex][shuffle.get(i)])) return i;
            }
        }
        return -1;
    }
}
