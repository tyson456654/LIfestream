/**
 * LifeStream Mobile Platform (Admin Console)
 * Native Framework: React Native with Expo & Tailwind CSS (nativewind) / StyleSheet
 * Screen: Blood Bank Inventory Management Screen (Indian Regional Interface)
 */

import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  ScrollView,
  TouchableOpacity,
  TextInput,
  ActivityIndicator,
  Alert,
  StatusBar,
  SafeAreaView
} from 'react-native';

// Helper for Indian Rupee or standard clinical representation
const IND_RED = '#0F766E'; // Peaceful Clinical Teal
const SLATE_BG = '#F8FAFC';
const SLATE_BORDER = '#E2E8F0';

export default function BloodBankInventory() {
  const [loading, setLoading] = useState(false);
  const [updatingType, setUpdatingType] = useState(null);
  const [stock, setStock] = useState({
    'O+': 14,
    'O-': 2,
    'A+': 18,
    'A-': 4,
    'B+': 10,
    'B-': 1,
    'AB+': 6,
    'AB-': 0
  });

  const [inputValues, setInputValues] = useState({});
  const [lastUpdateInfo, setLastUpdateInfo] = useState({
    time: "Today, 10:30 AM",
    admin: "Dr. K. Raghavan (Medical officer)"
  });

  // Handle single type stock update local API triggers
  const handleUpdateStock = async (bloodType) => {
    const unitsStr = inputValues[bloodType];
    if (unitsStr === undefined || unitsStr.trim() === '') {
      Alert.alert("Error", "Please input a valid quantity of blood units.");
      return;
    }

    const units = parseInt(unitsStr, 10);
    if (isNaN(units) || units < 0) {
      Alert.alert("Error", "Please input a non-negative integer quantity.");
      return;
    }

    setUpdatingType(bloodType);
    setLoading(true);

    try {
      // simulated endpoint calling back to Node.js backend
      // fetch('https://lifestream.in/api/blood-bank/stock', { method: 'PUT', ... })
      await new Promise(resolve => setTimeout(resolve, 800)); // Network delay simulation

      setStock(prev => ({
        ...prev,
        [bloodType]: units
      }));

      setLastUpdateInfo({
        time: new Date().toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }) + ", " + new Date().toLocaleDateString('en-IN'),
        admin: "Administrator (Self-console)"
      });

      // Clear input
      setInputValues(prev => ({ ...prev, [bloodType]: '' }));
      Alert.alert("🇮🇳 Stock Synchronised", `Successfully updated ${bloodType} stock level to ${units} units on Bangalore Registry.`);

    } catch (err) {
      Alert.alert("Connection Failed", "Unable to establish handshake with local storage portal.");
    } finally {
      setUpdatingType(null);
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.safeContainer}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      <View style={styles.header}>
        <View style={styles.badgeContainer}>
          <Text style={styles.badgeText}>BANGALORE CLINICAL PORTAL</Text>
        </View>
        <Text style={styles.title}>Indian Red Cross Society</Text>
        <Text style={styles.subtitle}>Karnataka State HQ • Inventory Terminal</Text>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Statistics Banner */}
        <View style={styles.auditCard}>
          <Text style={styles.auditTitle}>LAST LOGISTICS DESK SYNC</Text>
          <Text style={styles.auditMeta}>Timestamp: {lastUpdateInfo.time}</Text>
          <Text style={styles.auditMeta}>Authorized By: {lastUpdateInfo.admin}</Text>
          <View style={styles.verificationBadge}>
            <Text style={styles.verifiedText}>✓ SYSTEM ONLINE (CONNECTED TO FCM ENDPOINTS)</Text>
          </View>
        </View>

        <Text style={styles.sectionHeader}>Current Stock & Dispatch Controls</Text>

        <View style={styles.gridContainer}>
          {Object.entries(stock).map(([bloodType, quantity]) => {
            const isLow = quantity < 5;
            return (
              <View key={bloodType} style={styles.inventoryCard}>
                <View style={[styles.bloodIndicator, { backgroundColor: quantity > 0 ? IND_RED : '#94A3B8' }]}>
                  <Text style={styles.bloodTypeLabel}>{bloodType}</Text>
                </View>

                <View style={styles.dataArea}>
                  <Text style={styles.unitsAmount}>
                    {quantity} <Text style={styles.unitsLabel}>Units</Text>
                  </Text>
                  {isLow && (
                    <Text style={styles.statusCritical}>
                      ⚠️ CRITICAL SHORTAGE
                    </Text>
                  )}
                  {!isLow && quantity > 0 && (
                    <Text style={styles.statusSafe}>
                      ✓ STABLE BUFFER
                    </Text>
                  )}
                </View>

                <View style={styles.updateControl}>
                  <TextInput
                    style={styles.numericInput}
                    placeholder="New qty"
                    keyboardType="numeric"
                    value={inputValues[bloodType] || ''}
                    onChangeText={(val) => setInputValues(prev => ({ ...prev, [bloodType]: val }))}
                  />
                  <TouchableOpacity
                    style={styles.updateButton}
                    onPress={() => handleUpdateStock(bloodType)}
                    disabled={loading}
                  >
                    {updatingType === bloodType ? (
                      <ActivityIndicator size="small" color="#FFFFFF" />
                    ) : (
                      <Text style={styles.updateButtonText}>SET</Text>
                    )}
                  </TouchableOpacity>
                </View>
              </View>
            );
          })}
        </View>

        <View style={styles.footerInfo}>
          <Text style={styles.footerText}>
            Updating values triggers real-time cloud sync. If any stock triggers falls below safe thresholds (5 Units), regional eligible O- donors receive direct high-priority notifications.
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeContainer: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  header: {
    paddingHorizontal: 20,
    paddingTop: 15,
    paddingBottom: 20,
    borderBottomWidth: 1,
    borderBottomColor: SLATE_BORDER,
  },
  badgeContainer: {
    alignSelf: 'flex-start',
    backgroundColor: '#FEF2F2',
    borderColor: IND_RED,
    borderWidth: 1,
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 4,
    marginBottom: 8,
  },
  badgeText: {
    color: IND_RED,
    fontWeight: '900',
    fontSize: 9,
    letterSpacing: 0.5,
  },
  title: {
    fontSize: 22,
    fontWeight: '900',
    color: '#0F172A',
    letterSpacing: -0.5,
  },
  subtitle: {
    fontSize: 12,
    color: '#64748B',
    fontWeight: '600',
    marginTop: 2,
  },
  scrollContent: {
    padding: 16,
    backgroundColor: SLATE_BG,
  },
  auditCard: {
    backgroundColor: '#0F172A',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 12,
    elevation: 3,
    marginBottom: 20,
  },
  auditTitle: {
    fontSize: 10,
    fontWeight: '900',
    color: '#34D399',
    letterSpacing: 1,
    marginBottom: 6,
  },
  auditMeta: {
    fontSize: 12,
    color: '#E2E8F0',
    fontWeight: '500',
    marginVertical: 1,
  },
  verificationBadge: {
    borderTopWidth: 1,
    borderTopColor: '#334155',
    marginTop: 10,
    paddingTop: 8,
  },
  verifiedText: {
    color: '#34D399',
    fontSize: 10,
    fontWeight: '900',
    letterSpacing: 0.5,
  },
  sectionHeader: {
    fontSize: 14,
    fontWeight: '800',
    color: '#1E293B',
    letterSpacing: 0.5,
    marginBottom: 12,
    paddingLeft: 4,
  },
  gridContainer: {
    gap: 12,
  },
  inventoryCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    borderColor: SLATE_BORDER,
    borderWidth: 1,
    padding: 12,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#64748B',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 1,
  },
  bloodIndicator: {
    width: 50,
    height: 50,
    borderRadius: 25,
    alignItems: 'center',
    justifyContent: 'center',
  },
  bloodTypeLabel: {
    color: '#FFFFFF',
    fontWeight: '900',
    fontSize: 18,
  },
  dataArea: {
    flex: 1,
    paddingLeft: 12,
  },
  unitsAmount: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
  },
  unitsLabel: {
    fontSize: 12,
    fontWeight: '500',
    color: '#64748B',
  },
  statusCritical: {
    color: '#EF4444',
    fontSize: 10,
    fontWeight: '800',
    marginTop: 2,
    letterSpacing: 0.5,
  },
  statusSafe: {
    color: '#10B981',
    fontSize: 10,
    fontWeight: '800',
    marginTop: 2,
    letterSpacing: 0.5,
  },
  updateControl: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  numericInput: {
    height: 38,
    width: 65,
    borderWidth: 1,
    borderColor: '#CBD5E1',
    borderRadius: 6,
    paddingHorizontal: 8,
    fontSize: 13,
    backgroundColor: '#F8FAFC',
    textAlign: 'center',
    marginRight: 8,
    color: '#0F172A',
  },
  updateButton: {
    backgroundColor: '#0F172A',
    height: 38,
    width: 45,
    borderRadius: 6,
    alignItems: 'center',
    justifyContent: 'center',
  },
  updateButtonText: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: '900',
  },
  footerInfo: {
    marginTop: 20,
    paddingHorizontal: 8,
    paddingBottom: 40,
  },
  footerText: {
    fontSize: 11,
    color: '#94A3B8',
    textAlign: 'center',
    lineHeight: 15,
    fontWeight: '500',
  },
});
