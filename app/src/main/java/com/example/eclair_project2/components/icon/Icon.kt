package com.example.eclair_project2.components.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.eclair_project2.R

@Composable
fun SplashIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.splash),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun Icon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.icon),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun FavIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.favicon),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun AdaptiveIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.adaptive_icon),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun AccountIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.account),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun ArrowIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.arrow),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun SmallArrow(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.smallarrow),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun BlackArrow(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.blackarrow),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun BookIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.book),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun BookChosenIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.bookchosen),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun CommunityIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.community),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun CommunityChosenIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.communitychosen),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun HappyIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.happyicon),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun HomeIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.home),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun HomeChosenIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.homechosen),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun LoginIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.login),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun PenIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.pen),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun PenChosenIcon(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.penchosen),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun SmilingEmoji(modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.smilingemoji),
        contentDescription = null,
        modifier = modifier.size(30.dp)
    )
}