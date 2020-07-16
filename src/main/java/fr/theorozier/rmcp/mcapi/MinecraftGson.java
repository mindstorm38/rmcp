package fr.theorozier.rmcp.mcapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MinecraftGson {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
}
