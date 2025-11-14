package com.devusercode.data.di

import android.content.Context
import androidx.room.Room
import com.devusercode.core.domain.auth.repo.AuthRepository
import com.devusercode.core.domain.chat.repo.ChatRepository
import com.devusercode.core.domain.user.repo.UserRepository
import com.devusercode.data.auth.FirebaseAuthRepository
import com.devusercode.data.firebase.FirebaseChatRepository
import com.devusercode.data.firebase.FirebaseUserRepository
import com.devusercode.data.local.db.AppDatabase
import com.devusercode.data.local.db.dao.ConversationDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton fun provideDb(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides @Singleton
    fun provideRoom(@ApplicationContext ctx: Context): AppDatabase =
        Room
            .databaseBuilder(ctx, AppDatabase::class.java, "upchat.db")
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides fun provideConversationDao(db: AppDatabase): ConversationDao = db.conversationDao()

    @Provides @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        db: FirebaseDatabase,
    ): UserRepository = FirebaseUserRepository(auth, db)

    @Provides @Singleton
    fun provideChatRepository(
        db: FirebaseDatabase,
        conversationDao: ConversationDao,
    ): ChatRepository = FirebaseChatRepository(db, conversationDao)

    @Provides @Singleton
    fun provideAuthRepository(
        @ApplicationContext ctx: Context,
        auth: FirebaseAuth,
        db: FirebaseDatabase,
    ): AuthRepository = FirebaseAuthRepository(ctx, auth, db)
}
