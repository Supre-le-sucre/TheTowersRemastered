package me.PauMAVA.TTR.util;

import java.util.ArrayList;
import java.util.List;


import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class ItemBuilder {

    //Code made by Bistouri

    private ItemStack is;


    public ItemBuilder(Material m){
        this(m, 1);
    }

    public ItemBuilder(ItemStack is){
        this.is=is;
    }

    public ItemBuilder(Material m, int amount){
        is= new ItemStack(m, amount);
    }


    @SuppressWarnings("deprecation")
    public ItemBuilder(Material m, int amount, short durability){
        is = new ItemStack(m, amount);
        is.setDurability(durability);
    }

    public ItemBuilder(String effect,int time,int level, int red, int green, int blue) {
        is = new ItemStack(Material.POTION,1);
        PotionMeta potion = (PotionMeta)is.getItemMeta();
        potion.setColor(Color.fromRGB(red,green,blue));
        potion.addCustomEffect(new PotionEffect(PotionEffectType.getByName(effect),time,level), true);
        is.setItemMeta(potion);
    }

    public ItemBuilder(String Type) {
        is = new ItemStack(Material.POTION,1);
        PotionMeta potion = (PotionMeta)is.getItemMeta();
        if(Type.equals("Thick")) {
            potion.setBasePotionData(new PotionData(PotionType.THICK));
        }
        is.setItemMeta(potion);
    }

    public PotionData getPotionData() {
        ItemMeta im = is.getItemMeta();
        PotionData pd;
        if(im instanceof PotionMeta) {
            pd = ((PotionMeta) im).getBasePotionData();
        }else {
            pd = new PotionData(PotionType.AWKWARD);
        }

        return pd;
    }

    public ItemBuilder setAmount(int amount) {
        is.setAmount(amount);
        return this;
    }

    public ItemBuilder setName(String str) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(str);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta im = is.getItemMeta();
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setLore(int line,String lore){
        ItemMeta im = is.getItemMeta();
        List<String> list = new ArrayList<>();
        if(im.getLore() != null){
            list = im.getLore();
        }
        try {
            list.set(line, lore);
        }catch(Exception e){
            list.add(line,lore);
        }
        im.setLore(list);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setEnchant(Enchantment ench,int lvl) {
        ItemMeta im = is.getItemMeta();
        im.addEnchant(ench, lvl, false);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder hideEnchant() {
        ItemMeta im = is.getItemMeta();
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder clone(){
        return new ItemBuilder(is);
    }

    public ItemStack toItemStack(){
        return is;
    }
}
