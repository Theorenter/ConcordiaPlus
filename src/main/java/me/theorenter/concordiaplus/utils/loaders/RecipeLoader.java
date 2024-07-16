package me.theorenter.concordiaplus.utils.loaders;

import me.theorenter.concordiaplus.ConcordiaPlus;
import me.theorenter.concordiaplus.exceptions.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RecipeLoader {
    private final ConcordiaPlus plugin;

    public RecipeLoader(@NotNull final ConcordiaPlus plugin, @NotNull final String path) throws IOException {
        this.plugin = plugin;

        if (!(new File(plugin.getDataFolder() + File.separator + path).exists()))
            loadToDataFolder(path);
    }

    private void loadToDataFolder(@NotNull final String path) throws IOException {
        Matcher m = Pattern.compile("plugins/.+\\.jar$")
                .matcher(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        String plPath = null;
        while (m.find()) {
            plPath = m.group(0);
        }

        assert plPath != null;
        try (ZipFile pJAR = new ZipFile(plPath)) {
            String p = (path.replaceAll("\\\\", "\\/"));
            Pattern m2 = Pattern.compile("^" + p + "/.+$");

            Enumeration<? extends ZipEntry> zipEntries = pJAR.entries();
            while (zipEntries.hasMoreElements()) {
                String fileName = zipEntries.nextElement().getName();
                if (m2.matcher(fileName).find()) {
                    File pdFile = new File(plugin.getDataFolder() + File.separator + fileName);
                    if (!pdFile.exists())
                        plugin.saveResource(fileName, false);
                }
            }
        }
    }

    public void registerAll(@NotNull final String path) {
        File recipeDir = new File(plugin.getDataFolder() + File.separator + path);

        File[] recipeFiles = recipeDir.listFiles();

        if (recipeFiles == null || recipeFiles.length == 0) {
            plugin.getLogger().warning("No custom recipes found in ConcordiaPlus\\" + path + " folder!");
            return;
        }

        int registeredRecipes = 0;

        for (File recipeFile : recipeFiles) {
            try {
                registerRecipe(recipeFile);
                registeredRecipes++;
            } catch (NotFoundRecipeTypeException | IllegalRecipeTypeException | NotFoundMaterialException |
                     IllegalMaterialException | NotFoundIdentifierException | NotFoundIngredientException |
                     RecipeKeyAlreadyRegisteredException | IllegalItemTypeException | NotFoundItemTypeException |
                     NotFundResultItemException | NotFoundConcordiaItemException | IllegalShapeFormatException |
                     NotFoundRecipeShapeException | NotFoundIngredientCharacterException | IllegalCraftingBookCategoryException ex) {
                plugin.getLogger().severe("Error loading recipe from file \"" + recipeFile.getPath() + "\"! The recipe will not be registered.");
                plugin.getLogger().severe(ex.getMessage());
            }
        }
        plugin.getLogger().info("Successfully registered " + registeredRecipes + " / " + recipeFiles.length + " custom recipes.");
    }

    private void registerRecipe(@NotNull final File recipeFile) throws
            NotFoundRecipeTypeException,
            IllegalRecipeTypeException,
            IllegalShapeFormatException,
            NotFoundItemTypeException,
            NotFundResultItemException,
            NotFoundConcordiaItemException,
            NotFoundMaterialException,
            NotFoundIngredientException,
            IllegalMaterialException,
            NotFoundRecipeShapeException,
            IllegalItemTypeException,
            NotFoundIdentifierException,
            RecipeKeyAlreadyRegisteredException,
            NotFoundIngredientCharacterException,
            IllegalCraftingBookCategoryException {
        Recipe recipe;

        FileConfiguration recipeYAML = YamlConfiguration.loadConfiguration(recipeFile);

        String type = recipeYAML.getString("type");
        if (type == null) {
            plugin.getLogger().severe("Error while load recipe file \"" + recipeFile.getPath() + "\"");
            throw new NotFoundRecipeTypeException("Recipe type not found! (the \"type\" field is missing)");
        }

        switch (type) {
            case "SHAPED_RECIPE": {
                recipe = toShapedRecipe(recipeYAML);
                break;
            }
            case "SHAPELESS_RECIPE": {
            }
            default: {
                throw new IllegalRecipeTypeException("\"" + type + "\" is an unknown recipe type. Known types: SHAPED_RECIPE, SHAPELESS_RECIPE.");
            }
        }
        plugin.getServer().addRecipe(recipe);
    }

    private ShapedRecipe toShapedRecipe(@NotNull final FileConfiguration recipeYAML) throws
            NotFoundRecipeTypeException,
            NotFoundIdentifierException,
            RecipeKeyAlreadyRegisteredException,
            NotFundResultItemException,
            NotFoundConcordiaItemException,
            IllegalMaterialException,
            IllegalItemTypeException,
            NotFoundItemTypeException,
            NotFoundIngredientException,
            NotFoundRecipeShapeException,
            IllegalShapeFormatException,
            NotFoundIngredientCharacterException, IllegalCraftingBookCategoryException {
        ShapedRecipe recipe;

        // type
        String recipeType = recipeYAML.getString("type");
        if (recipeType == null)
            throw new NotFoundRecipeTypeException("recipeType");

        // NSK
        String recipeKey = recipeYAML.getString("recipe-key");
        if (recipeKey == null)
            throw new NotFoundIdentifierException("Unique recipe key not found! (the \"recipe-key\" field is missing)");

        NamespacedKey NSK = new NamespacedKey(plugin, recipeKey);
        if (plugin.getServer().getRecipe(NSK) != null)
            throw new RecipeKeyAlreadyRegisteredException("Recipe key \"" + recipeKey + "\" already registered! (You have duplicate keys somewhere)");

        // result item
        ConfigurationSection resultSection = recipeYAML.getConfigurationSection("result");
        if (resultSection == null)
            throw new NotFundResultItemException("Result item not found!");

        ItemStack result = toResult(resultSection);

        // shape
        List<String> shape = recipeYAML.getStringList("shaped-recipe.shape");
        if (shape.isEmpty())
            throw new NotFoundRecipeShapeException("Shape for recipe \"" + recipeKey + "\" not found!");

        if (shape.size() != 3)
            throw new IllegalShapeFormatException("The shape for the recipe \"" + recipeKey + "\" is incorrect. It should consist of 3 lines, but consists of " + shape.size() + "!");

        for (int i = 0; i < shape.size(); i++) {
            if (shape.get(i).length() != 3)
                throw new IllegalShapeFormatException("The shape for the recipe \"" + recipeKey + "\" is incorrect. The length " + (i + 1) + " of the shape line consists of " + shape.get(i).length() + " characters, but should consist of 3!");
        }

        // ingredients
        Map<Character, RecipeChoice> ingredientMap;
        ConfigurationSection ingredientsYAML = recipeYAML.getConfigurationSection("shaped-recipe.ingredients");
        if (ingredientsYAML == null)
            throw new NotFoundIngredientException("No ingredients found for recipe \"" + recipeKey + "\"!");

        ingredientMap = toChoiceMap(ingredientsYAML);

        // Finally!
        recipe = new ShapedRecipe(NSK, result);
        recipe.shape(shape.get(0), shape.get(1), shape.get(2));
        for (Character ingChar : ingredientMap.keySet())
            recipe.setIngredient(ingChar, ingredientMap.get(ingChar));

        // group
        String group = recipeYAML.getString("shaped-recipe.group");
        if (group != null) {
            recipe.setGroup(group);
        }

        // category
        String categoryYAML = recipeYAML.getString("shaped-recipe.category");
        if (categoryYAML != null) {
            try {
                CraftingBookCategory cbc = CraftingBookCategory.valueOf(categoryYAML);
                recipe.setCategory(cbc);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().severe("\"" + categoryYAML + "\" is not a valid recipe category!");
                throw new IllegalCraftingBookCategoryException("Available categories: BUILDING, EQUIPMENT, MISC, REDSTONE");
            }
        }
        return recipe;
    }

    @NotNull
    public ItemStack toResult(@NotNull final ConfigurationSection resultYAML) throws
            NotFoundItemTypeException,
            NotFoundIdentifierException,
            IllegalMaterialException,
            IllegalItemTypeException,
            NotFoundConcordiaItemException {
        ItemStack result;

        String resultType = resultYAML.getString("type");
        if (resultType == null)
            throw new NotFoundItemTypeException("The result item type was not found!");

        String ID = resultYAML.getString("ID");
        if (ID == null)
            throw new NotFoundIdentifierException("The result item ID was not found!");

        int amount = resultYAML.getInt("amount");
        if (amount == 0)
            amount = 1;

        switch (resultType) {
            case "CONCORDIAPLUS_ITEM": {
                result = plugin.getCustomItemStorage().getItem(ID);

                if (result == null) {
                    plugin.getLogger().severe("Failed to receive item under ID \"" + ID + "\" for recipe result item!");
                    throw new NotFoundConcordiaItemException("This ConcordiaItem may not be registered.");
                }
                break;
            }
            case "MINECRAFT_MATERIAL": {
                Material recipeMaterial;
                try {
                    recipeMaterial = Material.valueOf(ID);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalMaterialException("\"" + ID + "\" is not a valid material for result item!");
                }

                result = new ItemStack(recipeMaterial);
                break;
            }
            default: {
                plugin.getLogger().severe("\"" + resultType + "\" is not a valid material for result item!");
                throw new IllegalItemTypeException("Available types: MINECRAFT_MATERIAL, CONCORDIAPLUS_ITEM");
            }
        }

        result.setAmount(amount);
        return result;
    }

    @NotNull
    private Map<Character, RecipeChoice> toChoiceMap(@NotNull final ConfigurationSection shapedRecipeIngredientsYAML) throws
            NotFoundItemTypeException,
            NotFoundIngredientCharacterException,
            NotFoundIdentifierException,
            NotFoundConcordiaItemException,
            IllegalMaterialException,
            IllegalItemTypeException {
        Map<Character, RecipeChoice> ingredientMap = new HashMap<>();

        int i = 0;
        for (String charYAML : shapedRecipeIngredientsYAML.getKeys(false)) {
            i++;
            ConfigurationSection ingredientYAML = shapedRecipeIngredientsYAML.getConfigurationSection(charYAML);
            Character character = charYAML.toCharArray()[0];

            // char
            if (ingredientYAML == null)
                throw new NotFoundIngredientCharacterException("The shaped recipe ingredient not found!\n(" + (i + 1) + " ingredient)");

            // type
            String ingType = (String) ingredientYAML.getString("type");
            if (ingType == null)
                throw new NotFoundItemTypeException("The ingredient item type was not found!\n(" + (i + 1) + " ingredient)");

            // ID
            String ID = (String) ingredientYAML.get("ID");
            if (ID == null)
                throw new NotFoundIdentifierException("The ingredient item ID was not found!\n(" + (i + 1) + " ingredient)");

            // RecipeChoice
            RecipeChoice rc;
            switch (ingType) {
                case "CONCORDIAPLUS_ITEM": {
                    ItemStack itemStack = plugin.getCustomItemStorage().getItem(ID);

                    if (itemStack == null) {
                        plugin.getLogger().severe("Failed to receive item under ID \"" + ID + "\" for ingredient item!");
                        throw new NotFoundConcordiaItemException("This ConcordiaItem may not be registered.\n(" + (i + 1) + " ingredient)");
                    }
                    rc = new RecipeChoice.ExactChoice(itemStack);
                    break;
                }
                case "MINECRAFT_MATERIAL": {
                    Material ingMaterial;
                    try {
                        ingMaterial = Material.valueOf(ID);
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalMaterialException("\"" + ID + "\" is not a valid material for ingredient item!\n(" + (i + 1) + " ingredient)");
                    }

                    rc = new RecipeChoice.MaterialChoice(ingMaterial);
                    break;
                }
                default: {
                    plugin.getLogger().severe("\"" + ingType + "\" is not a valid material for ingredient item!");
                    throw new IllegalItemTypeException("Available types: MINECRAFT_MATERIAL, CONCORDIAPLUS_ITEM");
                }
            }
            ingredientMap.put(character, rc);
        }
        return ingredientMap;
    }
}
        /*
        for (int i = 0; i < shapedRecipeIngredientsYAML.size(); i++) {
            ConfigurationSection ingredientYAML = shapedRecipeIngredientsYAML.get(i);

            // char
            String charYAML = ingredientYAML.getString("character");
            if (charYAML == null)
                throw new NotFoundIngredientCharacterException("The shaped recipe ingredient does not indicate the symbol that the item corresponds to!\n("+(i+1)+" ingredient)");
            Character character = charYAML.toCharArray()[0];

            // type
            String ingType = (String) ingredientYAML.getString("type");
            if (ingType == null)
                throw new NotFoundItemTypeException("The ingredient item type was not found!\n("+(i+1)+" ingredient)");

            // ID
            String ID = (String) ingredientYAML.get("ID");
            if (ID == null)
                throw new NotFoundIdentifierException("The ingredient item ID was not found!\n("+(i+1)+" ingredient)");

            // RecipeChoice
            RecipeChoice rc;
            switch (ingType) {
                case "CONCORDIAPLUS_ITEM": {
                    ItemStack itemStack = plugin.getCustomItemStorage().getItem(ID);

                    if (itemStack == null) {
                        plugin.getLogger().severe("Failed to receive item under ID \"" + ID + "\" for ingredient item!");
                        throw new NotFoundConcordiaItemException("This ConcordiaItem may not be registered.\n(" + (i + 1) + " ingredient)");
                    }
                    rc = new RecipeChoice.ExactChoice(itemStack);
                    break;
                }
                case "MINECRAFT_MATERIAL": {
                    Material ingMaterial;
                    try {
                        ingMaterial = Material.valueOf(ID);
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalMaterialException("\"" + ID + "\" is not a valid material for ingredient item!\n(" + (i + 1) + " ingredient)");
                    }

                    rc = new RecipeChoice.MaterialChoice(ingMaterial);
                    break;
                }
                default: {
                    plugin.getLogger().severe("\"" + ingType + "\" is not a valid material for ingredient item!");
                    throw new IllegalItemTypeException("Available types: MINECRAFT_MATERIAL, CONCORDIAPLUS_ITEM");
                }
            }
            ingredientMap.put(character, rc);
        }
        return ingredientMap;
         */
