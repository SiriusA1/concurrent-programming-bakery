package hw2;
//Conor McGullam
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Customer implements Runnable {
    private Bakery bakery;
    private Random rnd;
    private List<BreadType> shoppingCart;
    private int shopTime;
    private int checkoutTime;

    /**
     * Initialize a customer object and randomize its shopping cart
     */
    public Customer(Bakery bakery) {
    	this.rnd = new Random();
        this.bakery = bakery;
        this.shoppingCart = new ArrayList<BreadType>();
        fillShoppingCart();
        this.shopTime = rnd.nextInt(50);
        this.checkoutTime = rnd.nextInt(50);
    }

    /**
     * Run tasks for the customer
     */
    public void run() {
        // TODO
        //getting bread
        int i = 0;
        while(i < this.shoppingCart.size()) {
            int breadnum;

            if(this.shoppingCart.get(i) == BreadType.RYE) {
                breadnum = 0;
                try {
                    this.bakery.allowRye.acquire();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if(this.shoppingCart.get(i) == BreadType.SOURDOUGH) {
                breadnum = 1;
                try {
                    this.bakery.allowSour.acquire();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                breadnum = 2;
                try {
                    this.bakery.allowWonder.acquire();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            this.bakery.takeBread(this.shoppingCart.get(i));
            //System.out.println("Getting bread " + breadnum);
            try {
                Thread.sleep(this.shopTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            if(breadnum == 0) {
                this.bakery.allowRye.release();
            } else if (breadnum == 1) {
                this.bakery.allowSour.release();
            } else {
                this.bakery.allowWonder.release();
            }
            
            i++;
        }

        //checkout
        try {
            this.bakery.allowCashier.acquire();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            this.bakery.allowSale.acquire();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.bakery.addSales(getItemsValue());
        //System.out.println("Checking out " + getItemsValue());
        try {
            Thread.sleep(this.checkoutTime);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        
        this.bakery.allowSale.release();
        this.bakery.allowCashier.release();
    }

    /**
     * Return a string representation of the customer
     */
    public String toString() {
        return "Customer " + hashCode() + ": shoppingCart=" + Arrays.toString(shoppingCart.toArray()) + ", shopTime=" + shopTime + ", checkoutTime=" + checkoutTime;
    }

    /**
     * Add a bread item to the customer's shopping cart
     */
    private boolean addItem(BreadType bread) {
        // do not allow more than 3 items, chooseItems() does not call more than 3 times
        if (shoppingCart.size() >= 3) {
            return false;
        }
        shoppingCart.add(bread);
        return true;
    }

    /**
     * Fill the customer's shopping cart with 1 to 3 random breads
     */
    private void fillShoppingCart() {
        int itemCnt = 1 + rnd.nextInt(3);
        while (itemCnt > 0) {
            addItem(BreadType.values()[rnd.nextInt(BreadType.values().length)]);
            itemCnt--;
        }
    }

    /**
     * Calculate the total value of the items in the customer's shopping cart
     */
    private float getItemsValue() {
        float value = 0;
        for (BreadType bread : shoppingCart) {
            value += bread.getPrice();
        }
        return value;
    }
}