package com.jclientmarket.presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.jclientmarket.JClientMarket;
import com.jclientmarket.R;
import com.jclientmarket.SocketTCP;
import com.jclientmarket.model.CartModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * com.jclientmarket.presenter in JClientMarket
 * Made by Floran Pagliai <floran.pagliai@gmail.com>
 * Started on 17/01/2014 at 16:38
 */

public class CartPresenter extends Activity {
    private ArrayList<CartModel> cart_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_layout);
        cart();

        Button productPageBtn = (Button)findViewById(R.id.productPage);
        productPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View actuelView) {
                Intent intent = new Intent(CartPresenter.this, ProductsPresenter.class);
                startActivity(intent);
                finish();
            }
        });
        Button cartPageBtn = (Button)findViewById(R.id.cartPage);
        cartPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View actuelView) {
                cart();
            }
        });
        Button logoutBtn = (Button)findViewById(R.id.logout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View actuelView) {
                SocketTCP.getInstance().send("logout");
                Intent intent = new Intent(CartPresenter.this, JClientMarket.class);
                startActivity(intent);
                finish();
            }
        });

        Button payBtn = (Button)findViewById(R.id.pay);
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View actuelView) {
                SocketTCP.getInstance().send("pay");
                AlertDialog.Builder adb = new AlertDialog.Builder(CartPresenter.this, AlertDialog.THEME_HOLO_LIGHT);
                adb.setTitle(getString(R.string.thx));
                adb.setMessage(getString(R.string.sum) + " " + totalCart() + " €");
                adb.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cart();
                    }
                });
                adb.show();
                Button payBtn = (Button)findViewById(R.id.pay);
                payBtn.setVisibility(View.INVISIBLE);
            }
        });


    }

    public void cart() {
        cart_ = new ArrayList<CartModel>();
        SocketTCP.getInstance().send("getcartcontent");
        String ret = SocketTCP.getInstance().receive();
        if (!ret.equalsIgnoreCase("cartEmpty")) {
            String token[] = ret.split("[|]");
            for (int i = 1 ; i != token.length ; i++) {
                String token2[] = token[i].split("[;]");
                this.cart_.add(new CartModel(Integer.parseInt(token2[0]), Integer.parseInt(token2[1]), Integer.parseInt(token2[2]), token2[3], Float.parseFloat(token2[4])));
            }
        }
        update();
    }

    public void update() {
        final ListView maListViewPerso = (ListView)findViewById(R.id.productsList);
        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;
        Button payBtn = (Button)findViewById(R.id.pay);
        payBtn.setVisibility(View.INVISIBLE);
        for (int i = 0 ; i != this.cart_.size() ; i++) {
            map = new HashMap<String, String>();
            map.put("id", String.valueOf(this.cart_.get(i).getId_()));
            map.put("designation", this.cart_.get(i).getDesignation_());
            map.put("quantity", "Quantité: " + String.valueOf(this.cart_.get(i).getQuantity_()));
            map.put("price", "Total :" + String.valueOf(this.cart_.get(i).getPrice_() * this.cart_.get(i).getQuantity_()) + " €");
            map.put("img", String.valueOf(R.drawable.del_panier));
            listItem.add(map);
        }
        if (this.cart_.size() > 0)
            payBtn.setVisibility(View.VISIBLE);
        payBtn.setText(getString(R.string.pay)+ " (" + totalCart() + " €)");
        SimpleAdapter mSchedule = new SimpleAdapter(this.getBaseContext(), listItem, R.layout.affichageitem,
                new String[]{"img", "designation", "quantity", "price"}, new int[]{R.id.img, R.id.titre, R.id.description, R.id.price});
        maListViewPerso.setAdapter(mSchedule);
        maListViewPerso.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                final HashMap<String, String> map = (HashMap<String, String>)maListViewPerso.getItemAtPosition(position);
                SocketTCP.getInstance().send("delfromcart;" + map.get("id"));
                String ret = SocketTCP.getInstance().receive();
                if (ret.equalsIgnoreCase("delfromcartOk")) {
                    if (cart_.get(position).getQuantity_() > 1)
                        cart_.get(position).setQuantity_(cart_.get(position).getQuantity_() - 1);
                    else
                        cart_.remove(position);
                    update();
                }
            }
        });
    }

    public int totalCart() {
        int sum = 0;
        for (int i = 0 ; i != this.cart_.size() ; i++) {
            sum += this.cart_.get(i).getQuantity_() * this.cart_.get(i).getPrice_();
        }
        return sum;
    }
}
