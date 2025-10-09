import secrets
import json
from datetime import datetime, timedelta, timezone
from typing import Optional
from jose import JWTError, jwt
from passlib.context import CryptContext
from fastapi import HTTPException, status, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from app.config import settings
from app.database import get_db
from app.models import User, OAuthClient, OAuthToken
from app.schemas import TokenData

# Настройка хеширования паролей
pwd_context = None

def get_pwd_context():
    global pwd_context
    if pwd_context is None:
        pwd_context = CryptContext(schemes=["pbkdf2_sha256"], deprecated="auto")
    return pwd_context

# Настройка OAuth
security = HTTPBearer()


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Проверка пароля"""
    return get_pwd_context().verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    """Хеширование пароля"""
    return get_pwd_context().hash(password)


def generate_client_credentials() -> tuple[str, str]:
    """Генерация client_id и client_secret"""
    client_id = secrets.token_urlsafe(32)
    client_secret = secrets.token_urlsafe(64)
    return client_id, client_secret


def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    """Создание JWT токена"""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=settings.access_token_expire_minutes)
    
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, settings.secret_key, algorithm=settings.algorithm)
    return encoded_jwt


def verify_token(token: str, credentials_exception) -> TokenData:
    """Проверка JWT токена"""
    try:
        payload = jwt.decode(token, settings.secret_key, algorithms=[settings.algorithm])
        username: str = payload.get("sub")
        client_id: str = payload.get("client_id")
        scope: str = payload.get("scope", "")
        
        if username is None and client_id is None:
            raise credentials_exception
            
        token_data = TokenData(username=username, client_id=client_id, scope=scope)
    except JWTError:
        raise credentials_exception
    return token_data


def get_user_by_username(db: Session, username: str) -> Optional[User]:
    """Получение пользователя по имени"""
    return db.query(User).filter(User.username == username).first()


def get_oauth_client(db: Session, client_id: str) -> Optional[OAuthClient]:
    """Получение OAuth клиента"""
    return db.query(OAuthClient).filter(OAuthClient.client_id == client_id).first()


def authenticate_user(db: Session, username: str, password: str) -> Optional[User]:
    """Аутентификация пользователя"""
    user = get_user_by_username(db, username)
    if not user:
        return None
    if not verify_password(password, user.hashed_password):
        return None
    return user


def authenticate_client(db: Session, client_id: str, client_secret: str) -> Optional[OAuthClient]:
    """Аутентификация OAuth клиента"""
    client = get_oauth_client(db, client_id)
    if not client:
        return None
    if not verify_password(client_secret, client.client_secret):
        return None
    return client


def create_oauth_token(
    db: Session, 
    client_id: str, 
    user_id: Optional[int] = None,
    scope: str = "read write",
    expires_in: int = 3600
) -> OAuthToken:
    """Создание OAuth токена"""
    # Генерируем токены
    access_token = secrets.token_urlsafe(64)
    refresh_token = secrets.token_urlsafe(64)
    
    # Создаем запись в базе данных
    oauth_token = OAuthToken(
        client_id=client_id,
        user_id=user_id,
        token_type="access_token",  # Добавляем token_type
        access_token=access_token,
        refresh_token=refresh_token,
        scope=scope,
        expires_at=datetime.now(timezone.utc) + timedelta(seconds=expires_in)
    )
    
    db.add(oauth_token)
    db.commit()
    db.refresh(oauth_token)
    
    return oauth_token


def get_oauth_token(db: Session, access_token: str) -> Optional[OAuthToken]:
    """Получение OAuth токена по access_token"""
    return db.query(OAuthToken).filter(OAuthToken.access_token == access_token).first()


async def get_current_user_from_token(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
) -> User:
    """Получение текущего пользователя из OAuth токена"""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    
    # Проверяем OAuth токен
    oauth_token = get_oauth_token(db, credentials.credentials)
    if not oauth_token:
        raise credentials_exception
    
    # Проверяем, не истек ли токен
    if oauth_token.expires_at < datetime.now(timezone.utc):
        raise credentials_exception
    
    # Получаем пользователя
    if oauth_token.user_id:
        user = db.query(User).filter(User.id == oauth_token.user_id).first()
        if not user:
            raise credentials_exception
        return user
    else:
        raise credentials_exception


async def get_current_client_from_token(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
) -> OAuthClient:
    """Получение текущего клиента из OAuth токена"""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    
    # Проверяем OAuth токен
    oauth_token = get_oauth_token(db, credentials.credentials)
    if not oauth_token:
        raise credentials_exception
    
    # Проверяем, не истек ли токен
    if oauth_token.expires_at < datetime.now(timezone.utc):
        raise credentials_exception
    
    # Получаем клиента
    client = get_oauth_client(db, oauth_token.client_id)
    if not client:
        raise credentials_exception
    
    return client
