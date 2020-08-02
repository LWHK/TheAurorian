package com.elseytd.theaurorian.TileEntities;

import com.elseytd.theaurorian.TABlocks;
import com.elseytd.theaurorian.TAMod;
import com.elseytd.theaurorian.Blocks.TABlock_Scrapper;
import com.elseytd.theaurorian.Recipes.ScrapperRecipe;
import com.elseytd.theaurorian.Recipes.ScrapperRecipeHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Scrapper_TileEntity extends TileEntityLockable implements ITickable {

	private NonNullList<ItemStack> heldItems = NonNullList.<ItemStack>withSize(3, ItemStack.EMPTY);
	private int craftProgress = 0;
	private boolean isCrafting = false;

	@Override
	public void update() {
		if (!this.getWorld().isRemote) {
			if (this.isCrafting) {

				if (this.getWorld().getTotalWorldTime() % 4 == 0) {
					int newval = this.craftProgress + 1;

					if (newval >= 100) {
						this.stopCrafting();
						this.doCraft();
					} else {
						this.craftProgress = newval;
					}
				}
			}

			ItemStack slot1 = this.heldItems.get(0);
			ItemStack slot3output = this.heldItems.get(2);

			if (getRecipeOutput(slot1) != null) {
				if (slot3output.isEmpty() || (getRecipeOutput(slot1).getItem() == slot3output.getItem() && slot3output.getCount() + getRecipeOutput(slot1).getCount() <= slot3output.getMaxStackSize())) {
					if (this.isCrafting != true) {
						this.isCrafting = true;
					}
				} else {
					stopCrafting();
				}
			} else if (this.isCrafting) {
				stopCrafting();
			}

		}
	}

	@SideOnly(Side.CLIENT)
	public boolean isCrafting() {
		return this.craftProgress > 0;
	}

	public ItemStack getRecipeOutput(ItemStack input) {
		for (ScrapperRecipe recipe : ScrapperRecipeHandler.allRecipes) {
			if (input.getItem() == recipe.getInput().getItem() && this.heldItems.get(1).getItem() == Item.getItemFromBlock(TABlocks.crystal)) {
				return recipe.getOutput().copy();
			}
		}
		return null;
	}

	private void doCraft() {
		ItemStack output = getRecipeOutput(this.heldItems.get(0));
		if (output != null) {
			this.heldItems.get(0).shrink(1);
			this.heldItems.get(1).shrink(1);
			if (this.heldItems.get(2).isEmpty()) {
				this.heldItems.set(2, output);
			} else if (this.heldItems.get(2).getItem() == output.getItem() && output.isStackable()) {
				this.heldItems.get(2).grow(output.getCount());
			}
		}
	}

	private void stopCrafting() {
		this.isCrafting = false;
		this.craftProgress = 0;
	}

	@Override
	public String getGuiID() {
		return "theaurorian:scrapper";
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new Scrapper_Container(playerInventory, this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.heldItems = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(nbt, this.heldItems);
		this.craftProgress = nbt.getInteger("CraftProgress");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		ItemStackHelper.saveAllItems(nbt, this.heldItems);
		nbt.setInteger("CraftProgress", (short) this.craftProgress);
		return nbt;
	}

	@Override
	public int getSizeInventory() {
		return this.heldItems.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.heldItems) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return this.heldItems.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.heldItems, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(this.heldItems, index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		ItemStack itemstack = this.heldItems.get(index);
		boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack) && ItemStack.areItemStackTagsEqual(stack, itemstack);
		this.heldItems.set(index, stack);
		if (stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
		if (!flag) {
			this.markDirty();
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		if (this.world.getTileEntity(this.pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		switch (index) {
		case 0:
			for (ScrapperRecipe recipe : ScrapperRecipeHandler.allRecipes) {
				if (recipe.getInput().getItem() == stack.getItem()) {
					return true;
				}
			}
			return false;
		case 1:
			if (Item.getItemFromBlock(TABlocks.crystal) == stack.getItem()) {
				return true;
			}

			return false;
		default:
		case 2:
			return false;
		}
	}

	@Override
	public void clear() {
		this.heldItems.clear();
	}

	@Override
	public String getName() {
		return "container." + TAMod.MODID + "." + TABlock_Scrapper.BLOCKNAME;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getField(int id) {
		switch (id) {
		default:
		case 0:
			return craftProgress;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch (id) {
		default:
		case 0:
			craftProgress = value;
			break;
		}
	}

	@Override
	public int getFieldCount() {
		return 1;
	}

}
