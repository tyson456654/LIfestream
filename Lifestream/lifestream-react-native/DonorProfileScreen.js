/**
 * LifeStream Mobile Platform (Donor Front-end Client)
 * Native Framework: React Native with Expo & Theme Stylesheets
 * Screen: Donor User Profile, Emergency Contacts & Past Donation History
 */

import React, { useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  ScrollView,
  TouchableOpacity,
  Image,
  SafeAreaView,
  StatusBar,
  Alert
} from 'react-native';

const CrimsonPrimary = '#0F766E'; // Peaceful Clinical Teal
const GoldAccent = '#14B8A6'; // Minty Teal Accent
const SlateDark = '#0F172A';
const SlateBorder = '#E2E8F0';

export default function DonorProfileScreen() {
  // Mock donor profile data adapted for Indian user base
  const [profile, setProfile] = useState({
    name: "Rajesh Kumar",
    bloodType: "O-", // Universal Donor
    phone: "+91 98765 43210",
    email: "rajesh.kumar@lifestream.in",
    city: "Bangalore, Karnataka",
    status: "Active (Eligible)",
    lastDonated: "65 Days ago",
    nextEligibleDate: "Immediate",
    emergencyContacts: [
      { name: "Priya Kumar", relationship: "Spouse", phone: "+91 98765 43211", isPrimary: true },
      { name: "Praneeth Reddy", relationship: "Friend", phone: "+91 94440 12345", isPrimary: false }
    ],
    donationHistory: [
      { id: '1', date: '2026-04-15', location: 'Indian Red Cross Blood Bank, Bangalore', type: 'Whole Blood', token: 'LSTREAM-IN-4932-O-', status: 'Completed' },
      { id: '2', date: '2025-11-20', location: 'Manipal Hospital, Old Airport Road', type: 'Whole Blood', token: 'LSTREAM-IN-8891-O-', status: 'Completed' },
      { id: '3', date: '2025-06-10', location: 'St. John’s Hospital, Bangalore', type: 'Platelets', token: 'L_VOUCH_910', status: 'Completed' }
    ]
  });

  const triggerCallEmergency = (contact) => {
    Alert.alert(
      "Emergency Protocol",
      `Initiate emergency speed-dial call to ${contact.name} (${contact.relationship})?`,
      [
        { text: "Cancel", style: "cancel" },
        { text: "Call Now", onPress: () => console.log(`Phoning ${contact.phone}`) }
      ]
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={CrimsonPrimary} />
      
      {/* 1. HERO HEADER AREA MAP */}
      <View style={styles.headerHero}>
        <View style={styles.headerTop}>
          <Text style={styles.bannerSubtitle}>LIFESTREAM INDIA PROTOCOL</Text>
        </View>

        <View style={styles.profileRow}>
          <View style={styles.avatarCircle}>
            <Text style={styles.avatarLetters}>RK</Text>
          </View>
          <View style={styles.profileMeta}>
            <Text style={styles.donorName}>{profile.name}</Text>
            <Text style={styles.donorLocation}>📍 {profile.city}</Text>
            <View style={styles.statPill}>
              <Text style={styles.statPillText}>✓ WHOLE BLOOD ACTIVE</Text>
            </View>
          </View>
          <View style={styles.bloodBadge}>
            <Text style={styles.bloodTitle}>O-</Text>
            <Text style={styles.bloodDesc}>Donor</Text>
          </View>
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContainer} showsVerticalScrollIndicator={false}>
        
        {/* 2. REGIONAL BIO & ELIGIBILITY */}
        <View style={styles.sectionCard}>
          <View style={styles.cardHeader}>
            <Text style={styles.cardTitle}>🇮🇳 Contact & Cooldown Ledger</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoField}>Mobile</Text>
            <Text style={styles.infoVal}>{profile.phone}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoField}>Email</Text>
            <Text style={styles.infoVal}>{profile.email}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoField}>Status</Text>
            <Text style={[styles.infoVal, { color: '#059669', fontWeight: '900' }]}>{profile.status}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoField}>Last Donated</Text>
            <Text style={styles.infoVal}>{profile.lastDonated}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoField}>Next eligible</Text>
            <Text style={[styles.infoVal, { color: CrimsonPrimary, fontWeight: '900' }]}>{profile.nextEligibleDate}</Text>
          </View>
        </View>

        {/* 3. EMERGENCY CONTACTS VIEW */}
        <Text style={styles.sectionHeading}>Emergency Quick-Alert Contacts</Text>
        <View style={styles.emergencyContainer}>
          {profile.emergencyContacts.map((contact, index) => (
            <TouchableOpacity 
              key={index} 
              style={[styles.emergencyCard, contact.isPrimary && styles.primaryEmergencyBorder]}
              onPress={() => triggerCallEmergency(contact)}
            >
              <View style={styles.emergencyLeft}>
                <Text style={styles.emergencyName}>
                  {contact.name} {contact.isPrimary && <Text style={styles.primaryContactLabel}>(Primary)</Text>}
                </Text>
                <Text style={styles.emergencyRelationship}>Relation: {contact.relationship}</Text>
                <Text style={styles.emergencyPhone}>{contact.phone}</Text>
              </View>
              <View style={styles.emergencyRight}>
                <View style={[styles.phoneCircle, { backgroundColor: contact.isPrimary ? CrimsonPrimary : SlateDark }]}>
                  <Text style={styles.phoneIconChar}>📞</Text>
                </View>
              </View>
            </TouchableOpacity>
          ))}
        </View>

        {/* 4. PAST DONATION VOUCHERS & ARCHIVE HISTORY */}
        <Text style={styles.sectionHeading}>Verified Donation & Replacement Voucher Log</Text>
        <View style={styles.historyContainer}>
          {profile.donationHistory.map((item) => (
            <View key={item.id} style={styles.historyCard}>
              <View style={styles.historyHeader}>
                <Text style={styles.historyDate}>📅 {item.date}</Text>
                <View style={styles.historyStatusBadge}>
                  <Text style={styles.historyStatusText}>COMPLETED</Text>
                </View>
              </View>
              <Text style={styles.historyLocation}>{item.location}</Text>
              <Text style={styles.historyType}>Type: {item.type}</Text>
              <View style={styles.replacementVoucherBox}>
                <Text style={styles.replacementVoucherLabel}>REPLACEMENT LOGISTICS TOKEN:</Text>
                <Text style={styles.replacementVoucherValue}>{item.token}</Text>
              </View>
            </View>
          ))}
        </View>

        <View style={styles.legalInfoBox}>
          <Text style={styles.legalInfoText}>
            All donation records on LifeStream India are secured via standard cryptographic hashing in compliance with National Blood Transfusion Council guidelines.
          </Text>
        </View>

      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  headerHero: {
    backgroundColor: CrimsonPrimary,
    paddingHorizontal: 20,
    paddingTop: 15,
    paddingBottom: 25,
    borderBottomLeftRadius: 20,
    borderBottomRightRadius: 20,
  },
  headerTop: {
    alignItems: 'center',
    marginBottom: 15,
  },
  bannerSubtitle: {
    color: '#FFF',
    fontSize: 9,
    fontFamily: 'monospace',
    letterSpacing: 1.5,
    fontWeight: '800',
    backgroundColor: 'rgba(0,0,0,0.2)',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 4,
  },
  profileRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  avatarCircle: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: 'rgba(255,255,255,0.25)',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: '#FFF',
  },
  avatarLetters: {
    color: '#FFFFFF',
    fontWeight: '800',
    fontSize: 22,
  },
  profileMeta: {
    flex: 1,
    paddingLeft: 14,
  },
  donorName: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: '900',
  },
  donorLocation: {
    color: 'rgba(255,255,255,0.85)',
    fontSize: 12,
    fontWeight: '600',
    marginTop: 2,
  },
  statPill: {
    backgroundColor: 'rgba(0,0,0,0.15)',
    alignSelf: 'flex-start',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 12,
    marginTop: 6,
  },
  statPillText: {
    color: '#FFFFFF',
    fontSize: 8,
    fontWeight: '900',
    letterSpacing: 0.5,
  },
  bloodBadge: {
    backgroundColor: '#FFFFFF',
    width: 54,
    height: 54,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 4,
  },
  bloodTitle: {
    color: CrimsonPrimary,
    fontWeight: '900',
    fontSize: 20,
  },
  bloodDesc: {
    color: '#64748B',
    fontSize: 8,
    fontWeight: '800',
    textTransform: 'uppercase',
  },
  scrollContainer: {
    padding: 16,
  },
  sectionHeading: {
    fontSize: 14,
    fontWeight: '900',
    color: SlateDark,
    marginTop: 18,
    marginBottom: 8,
    letterSpacing: 0.5,
    textTransform: 'uppercase',
    paddingLeft: 2,
  },
  sectionCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    borderColor: SlateBorder,
    borderWidth: 1,
    shadowColor: '#64748B',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 1,
    marginBottom: 6,
  },
  cardHeader: {
    borderBottomWidth: 1,
    borderBottomColor: SlateBorder,
    paddingBottom: 8,
    marginBottom: 10,
  },
  cardTitle: {
    fontSize: 14,
    fontWeight: '800',
    color: SlateDark,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 6,
  },
  infoField: {
    fontSize: 13,
    color: '#64748B',
    fontWeight: '600',
  },
  infoVal: {
    fontSize: 13,
    color: SlateDark,
    fontWeight: '700',
  },
  emergencyContainer: {
    gap: 10,
  },
  emergencyCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    padding: 12,
    borderColor: SlateBorder,
    borderWidth: 1,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#64748B',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.03,
    shadowRadius: 4,
    elevation: 1,
  },
  primaryEmergencyBorder: {
    borderColor: CrimsonPrimary,
    borderWidth: 1.5,
  },
  emergencyLeft: {
    flex: 1,
  },
  primaryContactLabel: {
    color: CrimsonPrimary,
    fontWeight: '900',
    fontSize: 11,
  },
  emergencyName: {
    fontSize: 13,
    fontWeight: '800',
    color: SlateDark,
  },
  emergencyRelationship: {
    fontSize: 11,
    color: '#64748B',
    fontWeight: '700',
    marginTop: 2,
  },
  emergencyPhone: {
    fontSize: 12,
    color: SlateDark,
    fontWeight: '700',
    marginTop: 2,
  },
  emergencyRight: {
    paddingLeft: 10,
  },
  phoneCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
  phoneIconChar: {
    fontSize: 14,
    color: '#FFF',
  },
  historyContainer: {
    gap: 10,
  },
  historyCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    borderColor: SlateBorder,
    borderWidth: 1,
    padding: 14,
    shadowColor: '#64748B',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.04,
    shadowRadius: 6,
    elevation: 1,
  },
  historyHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
    paddingBottom: 8,
    marginBottom: 8,
  },
  historyDate: {
    fontSize: 12,
    fontWeight: '800',
    color: SlateDark,
  },
  historyStatusBadge: {
    backgroundColor: '#CCFBF1',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 4,
  },
  historyStatusText: {
    color: '#0D9488',
    fontSize: 8,
    fontWeight: '900',
  },
  historyLocation: {
    fontSize: 13,
    fontWeight: '800',
    color: SlateDark,
    marginBottom: 2,
  },
  historyType: {
    fontSize: 11,
    color: '#64748B',
    fontWeight: '600',
  },
  replacementVoucherBox: {
    backgroundColor: '#FEF2F2',
    borderColor: CrimsonPrimary,
    borderWidth: 1,
    borderStyle: 'dashed',
    borderRadius: 6,
    padding: 8,
    marginTop: 10,
  },
  replacementVoucherLabel: {
    fontSize: 8,
    fontWeight: '900',
    color: CrimsonPrimary,
    letterSpacing: 0.5,
  },
  replacementVoucherValue: {
    fontSize: 11,
    fontWeight: '800',
    color: CrimsonPrimary,
    fontFamily: 'monospace',
    marginTop: 1,
  },
  legalInfoBox: {
    marginTop: 25,
    paddingBottom: 40,
    paddingHorizontal: 10,
  },
  legalInfoText: {
    fontSize: 10,
    color: '#94A3B8',
    textAlign: 'center',
    lineHeight: 14,
    fontWeight: '500',
  },
});
