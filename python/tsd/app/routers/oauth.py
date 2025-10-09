import json
from datetime import timedelta
from fastapi import APIRouter, Depends, HTTPException, status, Form
from sqlalchemy.orm import Session
from app.database import get_db
from app.models import User, OAuthClient, OAuthToken
from app.schemas import (
    OAuthClientCreate, 
    OAuthClient as OAuthClientSchema,
    OAuthClientResponse,
    TokenRequest, 
    TokenResponse,
    UserCreate,
    User as UserSchema
)
from app.oauth import (
    generate_client_credentials,
    get_password_hash,
    authenticate_user,
    authenticate_client,
    create_oauth_token,
    get_oauth_client,
    get_current_user_from_token,
    get_current_client_from_token
)
from app.config import settings

router = APIRouter(prefix="/oauth", tags=["oauth"])


@router.post("/register", response_model=OAuthClientResponse)
async def register_oauth_client(
    client_data: OAuthClientCreate, 
    db: Session = Depends(get_db)
):
    """Регистрация нового OAuth клиента"""
    client_id, client_secret = generate_client_credentials()
    
    # Создаем нового OAuth клиента
    oauth_client = OAuthClient(
        client_id=client_id,
        client_secret=get_password_hash(client_secret),
        client_name=client_data.client_name,
        redirect_uris=json.dumps(client_data.redirect_uris),
        scope=client_data.scope
    )
    
    db.add(oauth_client)
    db.commit()
    db.refresh(oauth_client)
    
    # Возвращаем клиента с plain text client_secret (только при создании)
    response_data = OAuthClientSchema.from_orm(oauth_client)
    return OAuthClientResponse(
        **response_data.model_dump(),
        client_secret=client_secret
    )


@router.post("/token", response_model=TokenResponse)
async def get_token(
    grant_type: str = Form(...),
    client_id: str = Form(...),
    client_secret: str = Form(None),
    username: str = Form(None),
    password: str = Form(None),
    scope: str = Form("read write"),
    db: Session = Depends(get_db)
):
    """Получение OAuth токена"""
    
    if grant_type == "client_credentials":
        # Client Credentials Grant
        if not client_secret:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="client_secret is required for client_credentials grant"
            )
        
        client = authenticate_client(db, client_id, client_secret)
        if not client:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid client credentials"
            )
        
        # Создаем токен для клиента
        oauth_token = create_oauth_token(
            db=db,
            client_id=client_id,
            scope=scope,
            expires_in=3600
        )
        
        return TokenResponse(
            access_token=oauth_token.access_token,
            token_type="Bearer",
            expires_in=3600,
            refresh_token=oauth_token.refresh_token,
            scope=scope
        )
    
    elif grant_type == "password":
        # Resource Owner Password Credentials Grant
        if not username or not password:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="username and password are required for password grant"
            )
        
        # Проверяем клиента
        client = get_oauth_client(db, client_id)
        if not client:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid client_id"
            )
        
        # Аутентифицируем пользователя
        user = authenticate_user(db, username, password)
        if not user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid username or password"
            )
        
        # Создаем токен для пользователя
        oauth_token = create_oauth_token(
            db=db,
            client_id=client_id,
            user_id=user.id,
            scope=scope,
            expires_in=3600
        )
        
        return TokenResponse(
            access_token=oauth_token.access_token,
            token_type="Bearer",
            expires_in=3600,
            refresh_token=oauth_token.refresh_token,
            scope=scope
        )
    
    else:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Unsupported grant_type"
        )


@router.post("/register-user", response_model=UserSchema)
async def register_user(
    user: UserCreate, 
    db: Session = Depends(get_db)
):
    """Регистрация нового пользователя"""
    # Проверяем, существует ли пользователь с таким именем
    db_user = db.query(User).filter(User.username == user.username).first()
    if db_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Username already registered"
        )
    
    # Проверяем, существует ли пользователь с таким email
    db_user = db.query(User).filter(User.email == user.email).first()
    if db_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email already registered"
        )
    
    # Создаем нового пользователя
    hashed_password = get_password_hash(user.password)
    db_user = User(
        username=user.username,
        email=user.email,
        hashed_password=hashed_password
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    
    return db_user


@router.get("/me", response_model=UserSchema)
async def get_current_user_info(
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение информации о текущем пользователе"""
    return current_user


@router.get("/client-info")
async def get_client_info(
    current_client: OAuthClient = Depends(get_current_client_from_token)
):
    """Получение информации о текущем клиенте"""
    return {
        "client_id": current_client.client_id,
        "client_name": current_client.client_name,
        "scope": current_client.scope,
        "is_active": current_client.is_active
    }
