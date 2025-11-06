package com.devusercode.upchat.di

import com.devusercode.core.domain.chat.repo.ChatRepository
import com.devusercode.core.domain.user.repo.UserRepository
import com.devusercode.data.firebase.FirebaseChatRepository
import com.devusercode.data.firebase.FirebaseUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides @Singleton fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    @Provides @Singleton fun provideDb(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides @Singleton
    fun provideUserRepository(auth: FirebaseAuth, db: FirebaseDatabase): UserRepository =
        FirebaseUserRepository(auth, db)

    @Provides @Singleton
    fun provideChatRepository(db: FirebaseDatabase): ChatRepository =
        FirebaseChatRepository(db)
}
