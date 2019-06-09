package com.jamesmhare.beveragefinder.retrievers;

import com.jamesmhare.beveragefinder.properties.ApplicationProperties;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class DrinkRetriever {

    private ApplicationProperties applicationProperties;
    private static final Logger LOGGER = Logger.getLogger(DrinkRetriever.class);

    public DrinkRetriever() {
        applicationProperties = new ApplicationProperties();
    }

    public String getDrink(String drinkRequest) {
        JSONObject drinkJSONResult = searchForDrink(drinkRequest);
        return buildDrinkFromSuggestion(drinkJSONResult, drinkRequest);
    }

    private JSONObject searchForDrink(String drink) {
        JSONObject output = new JSONObject();
        try {
            Client client = Client.create();
            String callToAPI = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=" + drink;
            WebResource webResource = client.resource(callToAPI);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
            if (response.getStatus() != 200) {
                LOGGER.error(response.getStatus());
                throw new Exception();
            }
            String result = response.getEntity(String.class);
            JSONParser parser = new JSONParser();
            output = (JSONObject) parser.parse(result);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return output;
    }

    private String buildDrinkFromSuggestion(JSONObject drinkJSONResult, String drinkRequest) {
        StringBuilder stringBuilder = new StringBuilder();
        JSONArray resultsArray = (JSONArray) drinkJSONResult.get("drinks");
        if (resultsArray != null) {
            JSONObject firstObject = (JSONObject) resultsArray.get(0);
            if (!firstObject.get("strDrink").toString().equalsIgnoreCase(drinkRequest.replace("&", " "))) {
                LOGGER.info("Request returned empty. " + drinkRequest.replace("&", " ") + " does not exist in the API.");
                stringBuilder.append("Sorry, the recipe for a " + drinkRequest.replace("&", " ") + " could not be found.");
            } else {
                stringBuilder.append(firstObject.get("strDrink") + ": ");
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure1"), (String) firstObject.get("strIngredient1"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure2"), (String) firstObject.get("strIngredient2"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure3"), (String) firstObject.get("strIngredient3"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure4"), (String) firstObject.get("strIngredient4"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure5"), (String) firstObject.get("strIngredient5"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure6"), (String) firstObject.get("strIngredient6"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure7"), (String) firstObject.get("strIngredient7"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure8"), (String) firstObject.get("strIngredient8"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure9"), (String) firstObject.get("strIngredient9"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure10"), (String) firstObject.get("strIngredient10"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure11"), (String) firstObject.get("strIngredient11"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure12"), (String) firstObject.get("strIngredient12"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure13"), (String) firstObject.get("strIngredient13"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure14"), (String) firstObject.get("strIngredient14"));
                extractIngredient(stringBuilder, (String) firstObject.get("strMeasure15"), (String) firstObject.get("strIngredient15"));
                stringBuilder.append("Serve in a " + firstObject.get("strGlass") + ".");
                stringBuilder.append("::::");
                stringBuilder.append(firstObject.get("strDrink") + ": " + firstObject.get("strInstructions"));
                stringBuilder.append("::::");
                if (!firstObject.get("strDrinkThumb").toString().isEmpty()) {
                    try {
                        URL url = new URL(firstObject.get("strDrinkThumb").toString());
                        InputStream is = url.openStream();
                        OutputStream os = new FileOutputStream("tmp/images/DrinkImage.jpg");

                        byte[] b = new byte[2048];
                        int length;

                        while ((length = is.read(b)) != -1) {
                            os.write(b, 0, length);
                        }
                        is.close();
                        os.close();
                        stringBuilder.append("true");
                        LOGGER.info("Drink image was saved.");
                    } catch (IOException exception) {
                        stringBuilder.append("false");
                        LOGGER.error("Drink image could not be saved." + exception.getMessage());
                    }
                } else {
                    stringBuilder.append("false");
                    LOGGER.info("Drink image did not exist in the API and therefore could not be saved.");
                }
            }
        } else {
            LOGGER.info("Request returned empty. " + drinkRequest.replace("&", " ") + " does not exist in the API.");
            stringBuilder.append("Sorry, the recipe for a " + drinkRequest.replace("&", " ") + " could not be found.");
        }
        return stringBuilder.toString().trim();
    }

    private void extractIngredient(StringBuilder stringBuilder, String measure, String ingredient) {
        if (!measure.isEmpty() && !measure.equals(" ")) {
            stringBuilder.append(measure);
        } if (!ingredient.isEmpty() && !ingredient.equals(" ")) {
            stringBuilder.append(" " + ingredient);
        } if ((!measure.isEmpty() && !measure.equals(" ")) || (!ingredient.isEmpty() && !ingredient.equals(" "))) {
            stringBuilder.append(", ");
        }
    }

}
