import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.*;

class Card {
    String suit, rank;
    int value;

    Card(String suit, String rank, int value) {
        this.suit = suit;
        this.rank = rank;
        this.value = value;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}

class Deck {
    private List<Card> cards;
    private Random rand = new Random();

    Deck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 11};

        cards = new ArrayList<>();
        for (String suit : suits) {
            for (int j = 0; j < ranks.length; j++) {
                cards.add(new Card(suit, ranks[j], values[j]));
            }
        }
        shuffle();
    }

    void shuffle() {
        Collections.shuffle(cards, rand);
    }

    Card drawCard() {
        return cards.remove(cards.size() - 1);
    }
}

class Player {
    List<Card> hand = new ArrayList<>();
    int balance;

    Player(int balance) {
        this.balance = balance;
    }

    void addCard(Card card) {
        hand.add(card);
    }
    
    int getHandValue() {
        int sum = 0, aces = 0;
        for (Card card : hand) {
            sum += card.value;
            if (card.rank.equals("Ace")) aces++;
        }
        while (sum > 21 && aces > 0) {
            sum -= 10;
            aces--;
        }
        return sum;
    }
}

class BlackjackGame extends JFrame {
    private Deck deck;
    private Player player, dealer;
    private JPanel gamePanel, betPanel, resultPanel;
    private JLabel balanceLabel, resultLabel, betAmountLabel;
    private JButton chip1, chip10, chip100, chip500, dealButton, hitButton, standButton, nextButton, playAgainButton;
    private int betAmount = 0;
    private boolean roundActive = false;
    private boolean revealDealer = false;  
    private boolean turnOver = false;    
    private boolean isDealerCardHidden = true; 

    BlackjackGame() {
        deck = new Deck();
        player = new Player(2500);
        dealer = new Player(0);

        setTitle("Blackjack Game");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new CardLayout());

        balanceLabel = new JLabel("Balance: " + player.balance);
        resultLabel = new JLabel("Place your bet", SwingConstants.CENTER);
        betAmountLabel = new JLabel("Bet Amount: 0");
        
        betPanel = new JPanel();
        betPanel.setBackground(new Color(0, 128, 0));
        chip1 = new JButton("1");
        chip10 = new JButton("10");
        chip100 = new JButton("100");
        chip500 = new JButton("500");
        dealButton = new JButton("Deal");
        betPanel.add(balanceLabel);
        betPanel.add(new JLabel("Bet: "));
        betPanel.add(chip1);
        betPanel.add(chip10);
        betPanel.add(chip100);
        betPanel.add(chip500);
        betPanel.add(betAmountLabel);
        betPanel.add(dealButton);
        
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCards(g);
            }
        };
        gamePanel.setBackground(new Color(0, 128, 0));
        
        resultPanel = new JPanel();
        resultLabel = new JLabel("Place your bet", SwingConstants.CENTER);
        nextButton = new JButton("Next Round");
        nextButton.addActionListener(e -> {
            if (turnOver) {
                evaluateResult();
            }
        });

        playAgainButton = new JButton("Play Again");
        playAgainButton.addActionListener(e -> {
            if (player.balance > 0) {
                resetGame(); 
            } else {
                resultLabel.setText("You don't have enough money to play again.");
            }
        });

        resultPanel.add(resultLabel);
        resultPanel.add(nextButton);
        resultPanel.add(playAgainButton);
        
        add(betPanel, "Bet");
        add(gamePanel, "Game");
        add(resultPanel, "Result");

        chip1.addActionListener(e -> placeBet(1));
        chip10.addActionListener(e -> placeBet(10));
        chip100.addActionListener(e -> placeBet(100));
        chip500.addActionListener(e -> placeBet(500));
        dealButton.addActionListener(e -> startGame());
    }

    void placeBet(int amount) {
        if (player.balance >= amount) {
            betAmount += amount;
            player.balance -= amount;
            balanceLabel.setText("Balance: " + player.balance);
            betAmountLabel.setText("Bet Amount: " + betAmount);
            repaint();
        }
    }

    void startGame() {
        if (betAmount == 0) {
            resultLabel.setText("Please place a bet first!");
            return;
        }
        roundActive = true;
        deck.shuffle();
        
        balanceLabel.setText("Balance: " + player.balance);
        isDealerCardHidden = true;
        
        player.hand.clear();
        dealer.hand.clear();

        player.addCard(deck.drawCard());
        player.addCard(deck.drawCard());
        dealer.addCard(deck.drawCard());
        dealer.addCard(deck.drawCard());

        hitButton = new JButton("Hit");
        hitButton.addActionListener(e -> hit());
        standButton = new JButton("Stand");
        standButton.addActionListener(e -> finishTurn());

        gamePanel.add(hitButton);
        gamePanel.add(standButton);

        resultLabel.setText("Player Hand: " + player.getHandValue());
        
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Game");
        repaint();
    }

    void resetGame() {
        betAmount = 0;
        betAmountLabel.setText("Bet Amount: 0");
        resultLabel.setText("Place your bet");

        player.hand.clear();
        dealer.hand.clear();

        gamePanel.remove(hitButton);
        gamePanel.remove(standButton);
        gamePanel.remove(nextButton);
        gamePanel.remove(playAgainButton);

        turnOver = false;
        isDealerCardHidden = true;

        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Bet");
        repaint();
    }

    void drawCards(Graphics g) {
        int xOffset = 20;
        int yOffset = 100;
        g.setColor(Color.WHITE);
        g.drawString("Player's Cards:", xOffset, yOffset - 20);
        for (Card card : player.hand) {
            drawCardRectangle(g, xOffset, yOffset);
            g.drawString(card.toString(), xOffset + 10, yOffset + 15);
            yOffset += 40;
        }

        xOffset = 350;
        yOffset = 100;
        g.drawString("Dealer's Cards:", xOffset, yOffset - 20);

        for (int i = 0; i < dealer.hand.size(); i++) {
            if (i == 0 && isDealerCardHidden) {
                g.setColor(Color.WHITE);  
                drawCardRectangle(g, xOffset, yOffset);
                g.setColor(Color.BLACK);
                g.drawString("Face Down", xOffset + 10, yOffset + 15);
            } else {
                drawCardRectangle(g, xOffset, yOffset);
                g.setColor(Color.BLACK);
                g.drawString(dealer.hand.get(i).toString(), xOffset + 10, yOffset + 15);
            }
            yOffset += 40;
        }
    }

    void drawCardRectangle(Graphics g, int x, int y) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, 70, 100);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, 70, 100);
    }

    void hit() {
        player.addCard(deck.drawCard());
        repaint();

        if (player.getHandValue() > 21) {
            finishTurn();
        }
    }

    void finishTurn() {
        isDealerCardHidden = false;
        repaint();

        while (dealer.getHandValue() < 17) {
            dealer.addCard(deck.drawCard());
        }

        repaint();
        turnOver = true;

        gamePanel.add(nextButton);
        gamePanel.revalidate();
        gamePanel.repaint();
    }

    void evaluateResult() {
        int playerValue = player.getHandValue();
        int dealerValue = dealer.getHandValue();

        if (playerValue > 21) {
            resultLabel.setText("You busted! Dealer wins.");
        } else if (dealerValue > 21) {
            resultLabel.setText("Dealer busted! You win!");
            player.balance += betAmount * 2;
        } else if (playerValue > dealerValue) {
            resultLabel.setText("You win!");
            player.balance += betAmount * 2;
        } else if (playerValue < dealerValue) {
            resultLabel.setText("Dealer wins.");
        } else {
            resultLabel.setText("It's a tie.");
            player.balance += betAmount;  
        }

        balanceLabel.setText("Balance: " + player.balance);

        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Result");

        if (player.balance <= 0) {
            playAgainButton.setEnabled(false);
            resultLabel.setText("You are out of money. Game Over.");
        }
    }
}
    
public class Blackjack {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BlackjackGame().setVisible(true));
    }
}