import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.event.*;
import java.awt.image.*;

class ClientGlobals{
  static boolean isPlayable = true;
  static boolean isSetting = true;
  static int playerID = 0;
  static int portValue = 8080;
  static String ipAddress = "localhost";
}

class Client extends JFrame implements Runnable {
  static Vector<Images> arrayImgs = new Vector<Images>();

  int currentForm[] = new int[3];
  Image player[][] = new Image[3][3];
  Scene sc = new Scene();
  Object sync = new Object();
  Scanner in;
  PrintStream out;
  static EnemyManager eMgr;

  public static void main (String[] args) {
    Socket socket = null;
    Scanner is = null/*new Scanner(System.in)*/;
    PrintStream os = null;

    new UserPreferences();
    while (ClientGlobals.isSetting)
      try { Thread.sleep(10); }
      catch (InterruptedException e) {}


    try {
      socket = new Socket(ClientGlobals.ipAddress, ClientGlobals.portValue);
      is = new Scanner(socket.getInputStream());
      os = new PrintStream(socket.getOutputStream());
    } catch (UnknownHostException e) {
        System.err.println("Don't know about host.");
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to host.");
    }

    System.out.println("Client connected to: " + ClientGlobals.ipAddress + "/" + ClientGlobals.portValue);

    String temp = is.nextLine();
    if (temp.startsWith("PLAYERID")){
      ClientGlobals.playerID = Character.getNumericValue(temp.charAt(9));
      System.out.println(">> MY ID: " + ClientGlobals.playerID);
    }
    else{
      System.err.println("Couldn't get Player's ID.");
      endGame();
    }

    Client client = new Client(is,os);
    new Thread(client).start();
  }

  Client(Scanner i, PrintStream o) {
    super("Untitled Game");
    
    this.in = i;
    this.out = o;

    setLocation(400,0);
    add(sc);
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_A:
        if (ClientGlobals.playerID == 1){
          switch(currentForm[GameGlobals.LEFT]){
          case GameGlobals.CIRCLE:
            changeForm('A',GameGlobals.TRIANGLE);
            break;
          case GameGlobals.TRIANGLE:
            changeForm('A',GameGlobals.SQUARE);
            break;
          case GameGlobals.SQUARE:
            changeForm('A',GameGlobals.CIRCLE);
            break;
          }
        }
        /*else{
          //GUITAR HERO
        }*/
          out.println("KEYPRESS A"+":"+currentForm[GameGlobals.LEFT]);
          break;
        case KeyEvent.VK_UP:
        case KeyEvent.VK_S:
        if (ClientGlobals.playerID == 1){
          switch(currentForm[GameGlobals.CENTER]){
          case GameGlobals.CIRCLE:
            changeForm('S',GameGlobals.TRIANGLE);
            break;
          case GameGlobals.TRIANGLE:
            changeForm('S',GameGlobals.SQUARE);
            break;
          case GameGlobals.SQUARE:
            changeForm('S',GameGlobals.CIRCLE);
            break;
          }
        }
        /*else{
          //GUITAR HERO
        }*/
          out.println("KEYPRESS S"+":"+currentForm[GameGlobals.CENTER]);
          break;
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_D:
        if (ClientGlobals.playerID == 1){
          switch(currentForm[GameGlobals.RIGHT]){
          case GameGlobals.CIRCLE:
            changeForm('D',GameGlobals.TRIANGLE);
            break;
          case GameGlobals.TRIANGLE:
            changeForm('D',GameGlobals.SQUARE);
            break;
          case GameGlobals.SQUARE:
            changeForm('D',GameGlobals.CIRCLE);
            break;
          }
        }
        /*else{
          //GUITAR HERO
        }*/
          out.println("KEYPRESS D"+":"+currentForm[GameGlobals.RIGHT]);
          break;
        }
      }
    });

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    pack();
    setVisible(true);
  }

  public void changeForm(Character position, int newShape){
    switch (position){
      case 'A':
        currentForm[GameGlobals.LEFT] = newShape;
        arrayImgs.set(1,new Images(player[GameGlobals.LEFT][currentForm[GameGlobals.LEFT]],GameGlobals.S_XL,GameGlobals.S_Y,GameGlobals.S_SIZE_XY,GameGlobals.S_SIZE_XY));
        break;
      case 'S':
        currentForm[GameGlobals.CENTER] = newShape;
        arrayImgs.set(2,new Images(player[GameGlobals.CENTER][currentForm[GameGlobals.CENTER]],GameGlobals.S_XC,GameGlobals.S_Y,GameGlobals.S_SIZE_XY,GameGlobals.S_SIZE_XY));
        break;
      case 'D':
        currentForm[GameGlobals.RIGHT] = newShape;
        arrayImgs.set(3,new Images(player[GameGlobals.RIGHT][currentForm[GameGlobals.RIGHT]],GameGlobals.S_XR,GameGlobals.S_Y,GameGlobals.S_SIZE_XY,GameGlobals.S_SIZE_XY));
        break;
    }
    sc.repaint();
  }

  public void run() {
    try {
      String response;
      Character position;
      int newShape;
      eMgr = new EnemyManager();
      eMgr.start();

      while (ClientGlobals.isPlayable) {
        response = in.nextLine();
        // ENEMY x:y:z
        if (response.startsWith("ENEMY")) {
          eMgr.queueEnemy(response);
        }
        // FORM x:y -- x is which position, y is what form it'll become
        else if (response.startsWith("FORM")) {
          position = response.charAt(5);
          newShape = Character.getNumericValue(response.charAt(7));
          changeForm(position,newShape);
        }
      }

    } catch (Exception e){
      System.out.println("Server terminated. Game will be shut down: " + e);
    }
    in.close();
    out.close();
    endGame();
  }

  public static void endGame(){
    System.out.println("Game over.");
    System.exit(0);
  } 

  class Images {
  	public Image graphic;
  	public int posX, posY, sizeX, sizeY;
  	Images (Image graphic, int posX, int posY, int sizeX, int sizeY) {
  		this.graphic = graphic;
  		this.posX = posX;
  		this.posY = posY;
  		this.sizeX = sizeX;
  		this.sizeY = sizeY;
  	}
  }

  class Scene extends JPanel {
  	Image background;

  	Scene() {
  		try {
  			background = ImageIO.read(new File("images/fundo.png"));
  			player[GameGlobals.LEFT][GameGlobals.CIRCLE] = ImageIO.read(new File("images/bCirc.png"));
  			player[GameGlobals.LEFT][GameGlobals.TRIANGLE] = ImageIO.read(new File("images/bTri.png"));
  			player[GameGlobals.LEFT][GameGlobals.SQUARE] = ImageIO.read(new File("images/bQuad.png"));
   			player[GameGlobals.CENTER][GameGlobals.CIRCLE] = ImageIO.read(new File("images/bCirc.png"));
  			player[GameGlobals.CENTER][GameGlobals.TRIANGLE] = ImageIO.read(new File("images/bTri.png"));
  			player[GameGlobals.CENTER][GameGlobals.SQUARE] = ImageIO.read(new File("images/bQuad.png"));
   			player[GameGlobals.RIGHT][GameGlobals.CIRCLE] = ImageIO.read(new File("images/bCirc.png"));
  			player[GameGlobals.RIGHT][GameGlobals.TRIANGLE] = ImageIO.read(new File("images/bTri.png"));
  			player[GameGlobals.RIGHT][GameGlobals.SQUARE] = ImageIO.read(new File("images/bQuad.png"));
  		} catch (IOException e) {
  			System.err.println("Could not load images.");
  		}

  		currentForm[GameGlobals.LEFT] = GameGlobals.CIRCLE;
  		currentForm[GameGlobals.CENTER] = GameGlobals.TRIANGLE;
  		currentForm[GameGlobals.RIGHT] = GameGlobals.SQUARE;

  		arrayImgs.add(new Images(background, 0, 0, GameGlobals.SCREEN_W, GameGlobals.SCREEN_H));
  		arrayImgs.add(new Images(player[GameGlobals.LEFT][currentForm[GameGlobals.LEFT]], GameGlobals.S_XL, GameGlobals.S_Y, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
  		arrayImgs.add(new Images(player[GameGlobals.CENTER][currentForm[GameGlobals.CENTER]], GameGlobals.S_XC, GameGlobals.S_Y, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
  		arrayImgs.add(new Images(player[GameGlobals.RIGHT][currentForm[GameGlobals.RIGHT]], GameGlobals.S_XR, GameGlobals.S_Y, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
  	}

  	public void paint (Graphics g) {
  		super.paint(g);
  		synchronized (sync) {
  			for (Images i : arrayImgs)
  				g.drawImage(i.graphic, i.posX, i.posY, i.sizeX, i.sizeY, this);
  		}
  	}

  	public Dimension getPreferredSize() {
  		return new Dimension (GameGlobals.SCREEN_W, GameGlobals.SCREEN_H);
  	}
  }

  class Enemy implements Runnable {
    Image enemy[] = new Image[3];
    int myIndex = 0;
    int enemyY = 0;
    
    Enemy(int left, int center, int right) {
      try {
        switch (left) {
        case GameGlobals.TRIANGLE: enemy[GameGlobals.LEFT] = ImageIO.read(new File("images/tri.png"));
          break;
        case GameGlobals.CIRCLE: enemy[GameGlobals.LEFT] = ImageIO.read(new File("images/circ.png"));
          break;
        case GameGlobals.SQUARE: enemy[GameGlobals.LEFT] = ImageIO.read(new File("images/quad.png"));
          break;
        }

        switch (center) {
        case GameGlobals.TRIANGLE: enemy[GameGlobals.CENTER] = ImageIO.read(new File("images/tri.png"));
          break;
        case GameGlobals.CIRCLE: enemy[GameGlobals.CENTER] = ImageIO.read(new File("images/circ.png"));
          break;
        case GameGlobals.SQUARE: enemy[GameGlobals.CENTER] = ImageIO.read(new File("images/quad.png"));
          break;
        }

        switch (right) {
        case GameGlobals.TRIANGLE: enemy[GameGlobals.RIGHT] = ImageIO.read(new File("images/tri.png"));
          break;
        case GameGlobals.CIRCLE: enemy[GameGlobals.RIGHT] = ImageIO.read(new File("images/circ.png"));
          break;
        case GameGlobals.SQUARE: enemy[GameGlobals.RIGHT] = ImageIO.read(new File("images/quad.png"));
          break;
        }
      } catch (IOException e) {
        System.err.println("Could not load images.");
      }

      synchronized (sync) {
        myIndex = arrayImgs.size();
        arrayImgs.add(new Images(enemy[GameGlobals.LEFT], GameGlobals.S_XL, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
        arrayImgs.add(new Images(enemy[GameGlobals.CENTER], GameGlobals.S_XC, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
        arrayImgs.add(new Images(enemy[GameGlobals.RIGHT], GameGlobals.S_XR, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
      }
    }

    public void run() {
      while (enemyY < GameGlobals.SCREEN_H) {
        try {
          Thread.sleep(GameGlobals.STEP_FREQ);
        } catch (InterruptedException e) {}

        enemyY += GameGlobals.STEP;

        arrayImgs.set(myIndex, new Images(enemy[GameGlobals.LEFT], GameGlobals.S_XL, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
        arrayImgs.set(myIndex+1, new Images(enemy[GameGlobals.CENTER], GameGlobals.S_XC, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));
        arrayImgs.set(myIndex+2, new Images(enemy[GameGlobals.RIGHT], GameGlobals.S_XR, enemyY, GameGlobals.S_SIZE_XY, GameGlobals.S_SIZE_XY));

        sc.repaint();
      }
    }
  }

  class EnemyManager extends Thread{      
    Vector<String> enemyVector = new Vector<String>();
    int index = 0;

    public void queueEnemy(String nextEnemy){
      enemyVector.add(nextEnemy);
    }

    private void createEnemy(String nextEnemy){
      int enemySigns[] = new int[3];
      enemySigns[GameGlobals.LEFT] = Character.getNumericValue(nextEnemy.charAt(6));
      enemySigns[GameGlobals.CENTER] = Character.getNumericValue(nextEnemy.charAt(8));
      enemySigns[GameGlobals.RIGHT] = Character.getNumericValue(nextEnemy.charAt(10));
      System.out.println(">>>> " + enemySigns[GameGlobals.LEFT] + ":" + enemySigns[GameGlobals.CENTER] + ":" + enemySigns[GameGlobals.RIGHT]);
      new Thread(new Enemy(enemySigns[GameGlobals.LEFT], enemySigns[GameGlobals.CENTER], enemySigns[GameGlobals.RIGHT])).start();
    }

    public void run(){
      while (ClientGlobals.isPlayable){
        if (index < enemyVector.size()){
          createEnemy(enemyVector.elementAt(index));
          index++;

          try {Thread.sleep(GameGlobals.SPAWN_TIME);}
          catch (InterruptedException e) {}
        }
      }
    }
  }
}

class UserPreferences extends JFrame implements ActionListener {
  JLabel text1 = new JLabel("Server Address:");
  JLabel text2 = new JLabel("Server Port:");
  JTextField ipText = new JTextField(15);
  JTextField portText = new JTextField(8);
  JButton setButton = new JButton("Set");

  UserPreferences() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new FlowLayout());

    mainPanel.add(text1);
    mainPanel.add(ipText);
    mainPanel.add(text2);
    mainPanel.add(portText);
    mainPanel.add(setButton);

    setButton.addActionListener(this);

    add(mainPanel);

    pack();

    setVisible(true);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  public void actionPerformed (ActionEvent e) {
    if(portText.getText().equals("")) {
      JOptionPane.showMessageDialog(this, "You must fill both fields!", "Error", JOptionPane.ERROR_MESSAGE);
    } else {
      try {
        ClientGlobals.ipAddress = ipText.getText();
        ClientGlobals.portValue = Integer.parseInt(portText.getText());
        ClientGlobals.isSetting = false;
        setVisible(false);
      } catch (Exception ex) {
        System.out.println("Invalid Port, " + ex);
        System.exit(0);
      }
    }
  }
}