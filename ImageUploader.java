package dsaab_project;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import javax.swing.Timer;


public class ImageUploader extends JFrame {
    private JLabel label;
    private JButton button;
    private JLabel imageLabel;
    private ImageIcon icon;
    private int[] coordinate = {-1, -1};  // 鼠标点击的最后的点坐标
    private LinkedList<int[]> list;  // 坐标序列
    private LinkedList<int[]> tempPathList;  // 临时路径列表
    private StringBuilder[] old ={null,null};
    private Robot robot;
    private int targetX, targetY ,size;
    private int now_imgX,now_imgY;
    private Get_path path;
    private boolean isMouseInWindow=true;
    private Timer timer;
    private Timer cursor_snap;

    public ImageUploader() {
        list = new LinkedList<>();
        tempPathList = new LinkedList<>();
        old=new StringBuilder[2];
        try {
            robot=new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        setTitle("Image Uploader");
        setSize(1000, 800); // Increased window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        label = new JLabel("Click the button to upload an image", SwingConstants.CENTER);
        panel.add(label, BorderLayout.NORTH);

        String info = JOptionPane.showInputDialog(null,"光标追踪边沿的邻域推荐输入：10-15（整数）","光标追踪边沿邻域",JOptionPane.WARNING_MESSAGE);
        size=Math.abs(Integer.parseInt(info));
        button = new JButton("Upload Image");
        button.addActionListener(e -> {
            String filePath = getSelectedFilePath();
            if (filePath != null) {
                icon = new ImageIcon(filePath);
                Image img = icon.getImage().getScaledInstance(imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(img));

                ImageReader reader = new ImageReader();
                path = new Get_path(reader.return_array(filePath));
                enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
                repaint(); // Repaint to update the UI

// 路径冷却方法：每五秒运行一次
                 timer= new Timer(5000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(!list.isEmpty() && !tempPathList.isEmpty() && isMouseInWindow){
                            if(old[0]!=null && old[1]!=null) {
                                int[] new_end=path.path_cooling(old,path.get_path_to_end(now_imgX, now_imgY));
                                StringBuilder[] tmp = path.get_path_to_end(new_end[0], new_end[1]);
                                String[] xpath = tmp[0].toString().split(",");
                                String[] ypath = tmp[1].toString().split(",");
                                for (int i = xpath.length - 1; i >= 0; i--) {
                                    int a = Integer.parseInt(xpath[i]);
                                    int b = Integer.parseInt(ypath[i]);
                                    addList(a, b);
                                }
                                path.get_all_path_from_start(new_end[0], new_end[1]);
                                label.setText("进行了路径冷却");
                                System.out.println("进行了一次路径冷却");
                            }
                                old = path.get_path_to_end(now_imgX, now_imgY);
                        }
                        repaint();
                    }
                });
                timer.start();

//  cursor_snap方法
                 cursor_snap = new Timer(2000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!list.isEmpty() && isMouseInWindow) { // 检查鼠标是否在窗口内
                            // 移动鼠标到目标坐标
                            int[] tmp = path.Cursor_Snap(now_imgX, now_imgY, size);
                            if (tmp[0] == 0) {
                                label.setText("鼠标已在图像边缘");
                            } else if (tmp[0] == 1) {
                                targetX = tmp[2] * imageLabel.getWidth() / icon.getIconWidth();
                                targetY = tmp[1] * imageLabel.getHeight() / icon.getIconHeight();

                                // 移动鼠标
                                Point screenLocation = imageLabel.getLocationOnScreen();
                                robot.mouseMove(screenLocation.x + targetX, screenLocation.y + targetY);
                                label.setText("鼠标已被自动移动到图像边缘");
                            }
                        } else if (!isMouseInWindow) {
                            label.setText("鼠标不在窗口内，暂停移动");
                        }
                        repaint(); // 强制重绘以更新标签文本
                    }
                });
                cursor_snap.start();
            }
        });
        panel.add(button, BorderLayout.SOUTH);

        imageLabel = new JLabel("", SwingConstants.CENTER) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (icon != null && getWidth() > 0 && getHeight() > 0) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(1));

                    // Draw permanent points and lines
                    for (int i = 0; i < list.size(); i++) {
                        int[] point = list.get(i);
                        int x = point[0] * imageLabel.getWidth() / icon.getIconWidth();
                        int y = point[1] * imageLabel.getHeight() / icon.getIconHeight();
                        // Draw a red dot centered at (x, y)
                        g2d.fillOval(x - 1, y - 1, 2, 2);
                        // Draw lines between points
                        if (i > 0) {
                            int[] prevPoint = list.get(i - 1);
                            int prevX = prevPoint[0] * imageLabel.getWidth() / icon.getIconWidth();
                            int prevY = prevPoint[1] * imageLabel.getHeight() / icon.getIconHeight();
                            g2d.drawLine(prevX, prevY, x, y);
                        }
                    }
                    if (!list.isEmpty() && !tempPathList.isEmpty()) {
                        // 固定的最后一个和第一个相连
                        int[] prelist = list.getLast();
                        int preX = prelist[0] * imageLabel.getWidth() / icon.getIconWidth();
                        int preY = prelist[1] * imageLabel.getHeight() / icon.getIconHeight();
                        int[] tmppoint = tempPathList.getFirst();
                        int tmpx = tmppoint[0] * imageLabel.getWidth() / icon.getIconWidth();
                        int tmpy = tmppoint[1] * imageLabel.getHeight() / icon.getIconHeight();
                        g2d.fillOval(tmpx - 1, tmpy - 1, 2, 2);
                        g2d.drawLine(preX, preY, tmpx, tmpy);
                    }

                    // Draw temporary path points and lines
                    for (int i = 1; i < tempPathList.size(); i++) {
                        int[] point = tempPathList.get(i);
                        int x = point[0] * imageLabel.getWidth() / icon.getIconWidth();
                        int y = point[1] * imageLabel.getHeight() / icon.getIconHeight();
                        // Draw a red dot centered at (x, y)
                        g2d.fillOval(x - 1, y - 1, 2, 2);
                        // Draw lines between points
                        if (i > 1) {
                            int[] prevPoint = tempPathList.get(i - 1);
                            int prevX = prevPoint[0] * imageLabel.getWidth() / icon.getIconWidth();
                            int prevY = prevPoint[1] * imageLabel.getHeight() / icon.getIconHeight();
                            g2d.drawLine(prevX, prevY, x, y);
                        }
                    }
                    g2d.dispose();
                }
            }
        };

        // Add mouse motion listener to track mouse movements
        imageLabel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (icon != null) {
                     now_imgX = e.getX() * icon.getIconWidth() / imageLabel.getWidth();
                     now_imgY = e.getY() * icon.getIconHeight() / imageLabel.getHeight();
                    // 此处导入求最优路径算法， 得到路径，加到tep_list里
                    tempPathList.clear();
                    // Check if coordinate has been assigned a valid value
                    if (coordinate[0] != -1 && coordinate[1] != -1 && path != null) {
                        StringBuilder[] tmp = path.get_path_to_end(now_imgX, now_imgY);
                        String[] xpath = tmp[0].toString().split(",");
                        String[] ypath = tmp[1].toString().split(",");
                        for (int i = xpath.length - 1; i >= 0; i--) {
                                int a = Integer.parseInt(xpath[i]);
                                int b = Integer.parseInt(ypath[i]);
                                tempPathList.add(new int[]{a, b});
                        }
                    }
                    // Repaint to update the UI
                    repaint();
                }
            }
        });

        // Add mouse listener to track mouse clicks
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (icon != null) {
                    int x = e.getX();
                    int y = e.getY();
                    // Convert screen coordinates to image coordinates
                    int imgX = x * icon.getIconWidth() / imageLabel.getWidth();
                    int imgY = y * icon.getIconHeight() / imageLabel.getHeight();
                    if (coordinate[0] != -1 && coordinate[1] != -1) {
                        StringBuilder[] tmp = path.get_path_to_end(imgX, imgY);
                        String[] xpath = tmp[0].toString().split(",");
                        String[] ypath = tmp[1].toString().split(",");
                        for (int i = xpath.length - 1; i >= 0; i--) {
                            int a = Integer.parseInt(xpath[i]);
                            int b = Integer.parseInt(ypath[i]);
                            addList(a, b);
                        }
                        path.get_all_path_from_start(imgX, imgY);
                        // Check if the current click is near the start point
                        if (close(list.getFirst()[0], list.getFirst()[1], imgX, imgY)) {
                            label.setText("已截图，路径冷却与光标移动结束运行");
                            timer.stop();
                            cursor_snap.stop();
                            displayPolygonScreenshot();
                        }
                    } else {
                        coordinate[0] = imgX;
                        coordinate[1] = imgY;
                        addList(imgX, imgY);
                        path.get_all_path_from_start(coordinate[0], coordinate[1]);
                    }
                }
            }
        });
        // 监听鼠标进入窗口事件
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isMouseInWindow = true;
                label.setText("鼠标已返回窗口内，继续移动");
                repaint();
            }
        });

        // 监听鼠标离开窗口事件
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                isMouseInWindow = false;
                label.setText("鼠标不在窗口内，暂停移动");
                repaint();
            }
        });
        panel.add(imageLabel, BorderLayout.CENTER);
        add(panel);
        setVisible(true);
    }

    // 得到图片的绝对路径
    public String getSelectedFilePath() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        }
        return null;
    }

    public void addList(int a, int b) {
        list.add(new int[]{a, b});
        repaint(); // Repaint to update the UI
    }

    // 判断是否接近
    public boolean close(int ax, int ay, int bx, int by) {
        return (Math.abs(ax - bx) + Math.abs(ay - by) <= 6);
    }
    private void displayPolygonScreenshot() {
        try {
            // 获取原始图像
            BufferedImage originalImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = originalImage.createGraphics();
            g2d.drawImage(icon.getImage(), 0, 0, null);
            g2d.dispose();

            // Create a Path2D object representing the polygon
            Path2D polygon = new Path2D.Double();
            boolean isFirstPoint = true;
            for (int[] point : list) {
                int x = point[0];
                int y = point[1];
                if (isFirstPoint) {
                    polygon.moveTo(x, y);
                    isFirstPoint = false;
                } else {
                    polygon.lineTo(x, y);
                }
            }
            polygon.closePath();

            // Create an Area from the polygon
            Area area = new Area(polygon);

            // Create a new BufferedImage for the cropped region
            BufferedImage croppedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            g2d = croppedImage.createGraphics();
            g2d.setClip(area);
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();

            // Trim the cropped image to remove transparent areas
            croppedImage = trim(croppedImage);

            // Save the cropped image to a file
            saveCroppedImage(croppedImage);

            // Create a new JFrame to display the screenshot
            JFrame screenshotFrame = new JFrame("Captured Screenshot");
            screenshotFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Increase the size of the frame
            screenshotFrame.setSize(800, 600); // Adjusted size

            screenshotFrame.setLocationRelativeTo(this);

            JLabel screenshotLabel = new JLabel(new ImageIcon(croppedImage));
            screenshotFrame.getContentPane().add(screenshotLabel, BorderLayout.CENTER);
            screenshotFrame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to capture screenshot.");
        }
    }
// 保存图片
    private void saveCroppedImage(BufferedImage croppedImage) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Cropped Image");
        fileChooser.setSelectedFile(new File("cropped_image.png"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                // Ensure the file has a .png extension
                if (!fileToSave.getName().toLowerCase().endsWith(".png")) {
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".png");
                }
                ImageIO.write(croppedImage, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Image saved successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save image.");
            }
        }
    }
    private BufferedImage trim(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int top = 0;
        int bottom = height - 1;
        int left = 0;
        int right = width - 1;

        outerLoopTop:
        for (; top < height; top++) {
            for (int x = 0; x < width; x++) {
                if ((image.getRGB(x, top) & 0xFF000000) != 0) {
                    break outerLoopTop;
                }
            }
        }

        outerLoopBottom:
        for (; bottom >= top; bottom--) {
            for (int x = 0; x < width; x++) {
                if ((image.getRGB(x, bottom) & 0xFF000000) != 0) {
                    break outerLoopBottom;
                }
            }
        }

        outerLoopLeft:
        for (; left < width; left++) {
            for (int y = top; y <= bottom; y++) {
                if ((image.getRGB(left, y) & 0xFF000000) != 0) {
                    break outerLoopLeft;
                }
            }
        }

        outerLoopRight:
        for (; right >= left; right--) {
            for (int y = top; y <= bottom; y++) {
                if ((image.getRGB(right, y) & 0xFF000000) != 0) {
                    break outerLoopRight;
                }
            }
        }

        return image.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

    
}

