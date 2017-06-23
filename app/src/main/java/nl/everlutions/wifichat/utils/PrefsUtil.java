package nl.everlutions.wifichat.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PrefsUtil implements Constants {

    private static final String TAG = PrefsUtil.class.getSimpleName();
    private static final String CANT_READ_PREF = "Context is null, cannot read preference: ";
    private static final String CANT_WRITE_PREF = "Context is null, cannot write preference: ";

    private static final ObjectMapper mObjectMapper;

    static {

        mObjectMapper = Utils.getObjectMapper();
    }

    public static boolean readBool(Context ctx, String key, boolean defValue) {

        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            return settings.getBoolean(key, defValue);
        } else {
            Log.e(TAG, CANT_READ_PREF + key);
        }

        return defValue;
    }

    static public void saveBool(Context ctx, String key, boolean value) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(key, value);

            editor.apply();
        } else {
            Log.e(TAG, CANT_WRITE_PREF + key);
        }
    }

    public static int readInt(Context ctx, String key, int defValue) {

        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            return settings.getInt(key, defValue);
        } else {
            Log.e(TAG, CANT_READ_PREF + key);
        }

        return defValue;
    }

    static public void saveInt(Context ctx, String key, int value) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(key, value);
            editor.apply();
        } else {
            Log.e(TAG, CANT_WRITE_PREF + key);
        }
    }

    public static float readFloat(Context ctx, String key, float defValue) {

        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            return settings.getFloat(key, defValue);
        } else {
            Log.e(TAG, CANT_READ_PREF + key);
        }

        return defValue;
    }

    static public void saveFloat(Context ctx, String key, float value) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat(key, value);
            editor.apply();
        } else {
            Log.e(TAG, CANT_WRITE_PREF + key);
        }
    }

    public static long readLong(Context ctx, String key, long defValue) {

        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            return settings.getLong(key, defValue);
        } else {
            Log.e(TAG, CANT_READ_PREF + key);
        }

        return defValue;
    }

    static public void saveLong(Context ctx, String key, long value) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(key, value);
            editor.apply();
        } else {
            Log.e(TAG, CANT_WRITE_PREF + key);
        }
    }

    public static String readString(Context ctx, String key, String defValue) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            return settings.getString(key, defValue);
        } else {
            Log.e(TAG, CANT_READ_PREF + key);
        }

        return defValue;
    }

    static public void saveString(Context ctx, String key, String value) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(key, value);
            editor.apply();
        } else {
            Log.e(TAG, CANT_WRITE_PREF + key);
        }
    }

    public static <T> T readObject(Context ctx, String key, Class<T> objectClass) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            String jsonObject = settings.getString(key, null);
            if (jsonObject != null) {
                try {
                    return mObjectMapper.readValue(jsonObject, objectClass);
                } catch (IOException e) {
                    Log.e(TAG, "readObject failed " + key + ": " + e.getMessage());
                }
            }
        } else {
            Log.e(TAG, CANT_READ_PREF + key);
        }
        return null;
    }

    public static void saveObject(Context ctx, String key, Object object) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            try {
                String value = mObjectMapper.writeValueAsString(object);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(key, value);
                editor.apply();
            } catch (IOException e) {
                Log.e(TAG, "saveObject failed: " + e.getMessage());
            }
        } else {
            Log.e(TAG, CANT_WRITE_PREF + key);
        }
    }

    /**
     * @param objectClass readObjectList(context, prefkey, new TypeReference<List<YourObject.class>>() {});
     */
    public static <T> T readObjectList(Context ctx, String key, TypeReference<T> objectClass) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            String jsonObject = settings.getString(key, null);
            if (jsonObject != null) {
                try {
                    return mObjectMapper.readValue(jsonObject, objectClass);
                } catch (IOException e) {
                    Log.e(TAG, "readObjectList failed " + key + ": " + e.getMessage());
                }
            }
        } else {
            Log.e(TAG, CANT_READ_PREF + key);
        }
        return null;
    }

    public static void saveObjectList(Context ctx, String key, ArrayList<?> objectList) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            try {
                String value = mObjectMapper.writeValueAsString(objectList);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(key, value);
                editor.apply();
            } catch (IOException e) {
                Log.e(TAG, "saveObjectList failed: " + e.getMessage());
            }
        } else {
            Log.e(TAG, CANT_WRITE_PREF + key);
        }
    }

    public static void saveHashMap(Context ctx, String key, HashMap<?, ?> objectList) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            try {
                String value = mObjectMapper.writeValueAsString(objectList);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(key, value);
                editor.apply();
            } catch (IOException e) {
                Log.e(TAG, "saveObjectList failed: " + e.getMessage());
            }
        } else {
            Log.e(TAG, CANT_WRITE_PREF + key);
        }
    }

    public static <T> T readHashMap(Context ctx, String key, Class<T> keyClass, Class<T> valueClass) {
        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                    Context.MODE_PRIVATE);
            String jsonObject = settings.getString(key, null);
            if (jsonObject != null) {
                try {
                    TypeFactory typeFactory = mObjectMapper.getTypeFactory();
                    MapType mapType = typeFactory.constructMapType(HashMap.class, keyClass, valueClass);
                    return mObjectMapper.readValue(jsonObject, mapType);

                } catch (IOException e) {
                    Log.e(TAG, "readHashMap failed " + key + ": " + e.getMessage());
                }
            }
        } else {
            Log.e(TAG, CANT_READ_PREF + key);
        }
        return null;
    }

    static public void clearPreferences(Context ctx, String preferencesName) {
        Log.w(TAG, "clearPreferences(" + preferencesName + ")");

        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(preferencesName, 0);
            SharedPreferences.Editor editor = settings.edit();

            // Clear all fields
            editor.clear();
            // Commit the edits!
            editor.apply();
        } else {
            Log.e(TAG, "Content is null, cannot clear preferences");
        }
    }

    static public void clearSinglePreference(Context ctx, String preferenceName) {
        Log.w(TAG, "clearPreferences(" + preferenceName + ")");

        if (ctx != null) {
            SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove(preferenceName).apply();

        } else {
            Log.e(TAG, "Content is null, cannot clear preferences");
        }
    }

    public static String getUUID(Context ctx, boolean getDeviceIDifAvailable) {
        String deviceID = Utils.getDeviceID(ctx);
        if (deviceID != null && !deviceID.isEmpty() && !deviceID.equalsIgnoreCase("null") && getDeviceIDifAvailable) {
            return deviceID;
        }
        SharedPreferences settings = ctx.getSharedPreferences(PREF_SETTINGS,
                Context.MODE_PRIVATE);
        String uuid = settings.getString("UUID", null);
        if (uuid != null) {
            return uuid;
        }
        uuid = UUID.randomUUID().toString().replace("-", "");
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("UUID", uuid);
        editor.apply();
        return uuid;
    }

}
