package com.elseytd.theaurorian.Compat.JEI;

import java.util.ArrayList;
import java.util.List;

import com.elseytd.theaurorian.TABlocks;
import com.elseytd.theaurorian.Recipes.ScrapperRecipe;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ScrapperRecipeWrapper implements IRecipeWrapper {

	private final List<ItemStack> INPUTS;
	private final ItemStack OUTPUT;

	public ScrapperRecipeWrapper(ScrapperRecipe recipe) {
		this.INPUTS = new ArrayList<>();
		INPUTS.add(recipe.getInput());
		INPUTS.add(new ItemStack(Item.getItemFromBlock(TABlocks.crystal)));
		this.OUTPUT = recipe.getOutput();
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, INPUTS);
		ingredients.setOutput(VanillaTypes.ITEM, OUTPUT);
	}

}
