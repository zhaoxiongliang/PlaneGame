package com.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by liang on 2017/4/24.
 */

public class planegame extends SurfaceView implements SurfaceHolder.Callback,Runnable,View.OnTouchListener{

    private Bitmap my;                      //自己飞机
    private Bitmap enemy;                   //敌人飞机
    private Bitmap bullet;                  //子弹
    private Bitmap background;              //背景
    private Bitmap explore;                 //爆炸
    private int display_w;                  //屏幕宽
    private int display_h;                  //屏幕高
    private Bitmap erjihuancun;             //二级缓存
    private ArrayList<gameImage> gameImages=new ArrayList<>();       //图片数组
    private ArrayList<bullet> bullets=new ArrayList<>();


    public planegame(Context context) {
        super(context);
        getHolder().addCallback(this);
        this.setOnTouchListener(this);          //事件注册
    }

    public void init(){                     //加载图片
        my= BitmapFactory.decodeResource(getResources(),R.drawable.hero);
        enemy=BitmapFactory.decodeResource(getResources(),R.drawable.enemy1);
        bullet=BitmapFactory.decodeResource(getResources(),R.drawable.bullet1);
        background=BitmapFactory.decodeResource(getResources(),R.drawable.background);
        explore=BitmapFactory.decodeResource(getResources(),R.drawable.enemy1_down1);

        erjihuancun=Bitmap.createBitmap(display_w,display_h, Bitmap.Config.ARGB_8888);
        gameImages.add(new backgroundImage(background));
        gameImages.add(new planeImage(my));
        gameImages.add(new enemyImage(enemy));
    }


    private interface gameImage{            //自定义图片接口

        public Bitmap getbitmap();
        public int getX();
        public int getY();
    }

    private class backgroundImage implements gameImage{

        Bitmap bg;
        Bitmap newbitmap;
        private int height=0;

        private backgroundImage(Bitmap bg){
            this.bg=bg;                                               //创建背景图片信息
            newbitmap=Bitmap.createBitmap(display_w,display_h, Bitmap.Config.ARGB_8888);

        }

        @Override
        public Bitmap getbitmap() {

            Paint p=new Paint();
            Canvas canvas=new Canvas(newbitmap);
            canvas.drawBitmap(bg,new Rect(0,0,bg.getWidth(),bg.getHeight()),        //绘制当前背景滚动位置
                    new Rect(0,height,bg.getWidth(),bg.getHeight()+height),p);
            canvas.drawBitmap(bg,new Rect(0,0,bg.getWidth(),bg.getHeight()),        //绘制下一页背景滚动位置
                    new Rect(0,-display_h,bg.getWidth(),height),p);

            height++;
            if(height==display_h){
                height=0;
            }
            return newbitmap;
        }

        @Override
        public int getX() {

            return 0;
        }

        @Override
        public int getY() {

            return 0;
        }
    }

    private class planeImage implements gameImage{

        private Bitmap my;
        private int x;
        private int y;
        private int width;
        private int height;
        private Bitmap bitmap;
        private int index=0;
        private int num=0;
        private List<Bitmap> bitmaps=new ArrayList<Bitmap>();

        public int getWidth(){

            return width;
        }

        public int getHeight(){

            return height;
        }

        private planeImage(Bitmap my){

            this.my=my;

            //向图片数组加入飞机图片
            bitmaps.add(Bitmap.createBitmap(my,0,0,my.getWidth()/2,my.getHeight()));
            bitmaps.add(Bitmap.createBitmap(my,(my.getWidth()/2)*1,0,my.getWidth()/2,my.getHeight()));

            //初始自己飞机的位置
            x=display_w-(my.getWidth()/2)*3;
            y=display_h-my.getHeight()+10;

            //得到飞机的高和宽
            width=my.getWidth()/2;
            height=my.getHeight();

        }

        @Override
        public Bitmap getbitmap() {

            //从图片数组中取出照片
             Bitmap bitmap = bitmaps.get(index);
            //当背景刷新5次后飞机刷新1次
            if(num==5) {
                index++;
                if (index == bitmaps.size()) {
                    index = 0;
                }
                num=0;
            }
            num++;

            return bitmap;
        }


        @Override
        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        @Override
        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    private class enemyImage implements gameImage{

        private Bitmap enemy=null;
        private List<Bitmap> bitmaps=new ArrayList<Bitmap>();
        private int index=0;
        private int x;
        private int y;
        private int num;
        private int width;
        private int height;


        private enemyImage(Bitmap enemy){

            this.enemy=enemy;
            bitmaps.add(Bitmap.createBitmap(enemy,0,0,enemy.getWidth(),enemy.getHeight()));

            Random ran=new Random();
            x=ran.nextInt(display_w-(enemy.getWidth()/4));
            y=-enemy.getHeight();

            width=enemy.getWidth();
            height=enemy.getHeight();
        }


        @Override
        public Bitmap getbitmap() {

            Bitmap bitmap=bitmaps.get(index);
            if(num==7) {
                index++;
                if (index == bitmaps.size()) {
                    index = 0;
                }
                num=0;
            }
            y+=10;
            num++;
            if(y>display_h){
                gameImages.remove(this);
            }

            return bitmap;
        }

        private boolean state=false;

        public void gethurt(ArrayList<bullet> bullets){

            if(!state){
                for (gameImage bullet : (List<gameImage>) bullets.clone()) {

                    if (bullet.getX() > x && bullet.getY() > y && bullet.getX() < x + width && bullet.getY() < y + height) {
                        Log.i("APP,TAG", "击中");
                        bullets.remove(bullet);
                        gameImages.remove(this);
                        state = true;
                        break;
                    }
                }
            }

        }

        @Override
        public int getX() {

            return x;
        }

        @Override
        public int getY() {

            return y;
        }
    }

    private class bullet implements gameImage{

        private Bitmap bullet;
        private planeImage plane;
        private int x;
        private int y;

        private bullet(Bitmap bullet,planeImage plane){

            this.bullet=bullet;
            this.plane=plane;

            x=(plane.getX()+plane.getWidth()/2)-8;
            y=plane.getY()-bullet.getHeight();

        }

        @Override
        public Bitmap getbitmap() {
            y-=50;

            if(y<0){
                bullets.remove(this);
            }

            return bullet;
        }

        @Override
        public int getX() {

            return x;
        }

        @Override
        public int getY() {

            return y;
        }
    }


    private boolean state=false;
    SurfaceHolder holder=null;


    @Override
    public void run() {                                      //绘制出图片数组里的照片

        Paint p1=new Paint();
        int enemy_num=0;
        int bullet_num=0;

        try {
            while (state){

                if(selectplane!=null){
                    if(bullet_num==10) {
                        bullets.add(new bullet(bullet, selectplane));
                        bullet_num=0;
                    }
                    bullet_num++;
                }

                Canvas canvas=holder.lockCanvas();
                Canvas newcanvas=new Canvas(erjihuancun);

                for(gameImage image:(List<gameImage>)gameImages.clone()){           //克隆循环

                    if(image instanceof enemyImage){
                        ((enemyImage)image).gethurt(bullets);

                    }

                    newcanvas.drawBitmap(image.getbitmap(),image.getX(),image.getY(),p1);

                }

                if(enemy_num==20){
                    enemy_num=0;
                    gameImages.add(new enemyImage(enemy));
                }
                enemy_num++;

                for(gameImage image:(List<bullet>)bullets.clone()){
                    newcanvas.drawBitmap(image.getbitmap(),image.getX(),image.getY(),p1);
                }

                canvas.drawBitmap(erjihuancun,0,0,p1);
                holder.unlockCanvasAndPost(canvas);
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        display_w=width;
        display_h=height;
        this.holder=holder;

        init();
        state=true;
        new Thread(this).start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        state=false;
    }

    planeImage selectplane;                    //选中的飞机图片

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(event.getAction()==MotionEvent.ACTION_DOWN){         //手按下屏幕
            for(gameImage gameImage:gameImages){                //循环找图片
                if(gameImage instanceof planeImage){            //判断图片是不是飞机图片
                    planeImage plane=(planeImage) gameImage;
                    if(plane.getX()<event.getX()                //判断手是否点中飞机
                            &&plane.getY()<event.getY()
                            &&plane.getX()+plane.getWidth()>event.getX()
                            &&plane.getY()+plane.getHeight()>event.getY()){

                        selectplane=plane;

                    }else {
                        selectplane=null;
                    }

                    break;
                }
            }
        }else if(event.getAction()==MotionEvent.ACTION_MOVE){
            //修改移动位置
            selectplane.setX((int)event.getX()-(selectplane.getWidth())/2);
            selectplane.setY((int)event.getY()-(selectplane.getHeight())/2);

        }else if(event.getAction()==MotionEvent.ACTION_UP){
            //手抬起后不选中飞机
            selectplane=null;
        }
        return true;
    }


}
