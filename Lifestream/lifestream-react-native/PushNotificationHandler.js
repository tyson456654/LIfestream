/**
 * LifeStream Push Notification Manager
 * Framework: React Native with Expo Notifications SDK & Firebase (FCM) configurations
 * Purpose: Setup notification listeners, display high-priority heads-up banners, and navigate upon interaction.
 */

import React, { useEffect, useRef, useState } from 'react';
import { Platform, StyleSheet, Text, View, Alert, Button } from 'react-native';
import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';

// Configure default notification behaviors for heads-up alerts
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

export default function PushNotificationHandler({ children }) {
  const [expoPushToken, setExpoPushToken] = useState('');
  const [notification, setNotification] = useState(false);
  const notificationListener = useRef();
  const responseListener = useRef();

  useEffect(() => {
    // 1. Register for high-priority push credentials
    registerForPushNotificationsAsync().then(token => {
      if (token) {
        setExpoPushToken(token);
        console.log("LifeStream India registered device token:", token);
        // Here, send the token to your Node.js server database:
        // fetch('https://lifestream.in/api/users/update-token', { method: 'POST', body: JSON.stringify({ token }) })
      }
    });

    // 2. Listener triggered when notification arrives while the app is in the foreground
    notificationListener.current = Notifications.addNotificationReceivedListener(notification => {
      setNotification(notification);
      
      const { title, body } = notification.request.content;
      const data = notification.request.content.data;

      // Handle custom urgent logistics actions
      if (data && data.type === 'URGENT_LOGISTICS_MATCH') {
        Alert.alert(
          "🚨 " + title,
          body,
          [
            { text: "Dismiss", style: "cancel" },
            { 
              text: "EXPLORE & HELP", 
              onPress: () => {
                // Navigate to screen in your React Native router, e.g. Navigation.navigate('RequestFeed')
                console.log(`Navigating to request ${data.requestId} at ${data.hospitalName}`);
              } 
            }
          ]
        );
      }
    });

    // 3. Listener triggered when user clicks or interacts with the push notification
    responseListener.current = Notifications.addNotificationResponseReceivedListener(response => {
      const data = response.notification.request.content.data;
      console.log("User interacted with notification. Data payload:", data);

      if (data && data.requestId) {
        // Handle deep-linking details
        Alert.alert("Deep Link Triggered", `Redirecting to matching dispatch request id: ${data.requestId}`);
      }
    });

    return () => {
      Notifications.removeNotificationSubscription(notificationListener.current);
      Notifications.removeNotificationSubscription(responseListener.current);
    };
  }, []);

  return (
    <View style={{ flex: 1 }}>
      {children}
    </View>
  );
}

/**
 * Native Device Handshake Logic to Request Push Notification Permissions & Retrieve Token.
 * Specifically handles Android 13+ Notification Permission channels.
 */
async function registerForPushNotificationsAsync() {
  let token;

  if (Platform.OS === 'android') {
    // Set up dedicated notification channel for urgent logistics dispatches
    await Notifications.setNotificationChannelAsync('urgent-logistics', {
      name: 'Urgent Blood Dispatches',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#0F766E', // Peaceful clinical teal lights
    });
  }

  if (Device.isDevice) {
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;
    
    // Request permission if not already granted
    if (existingStatus !== 'granted') {
      const { status } = await Notifications.requestPermissionsAsync();
      finalStatus = status;
    }
    
    if (finalStatus !== 'granted') {
      console.warn('Notification permission denied by user! No tokens will be generated.');
      return;
    }
    
    // Get device token
    token = (await Notifications.getExpoPushTokenAsync()).data;
  } else {
    console.log('Must use physical Android device for real-time Push Notifications.');
  }

  return token;
}
